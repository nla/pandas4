package pandas.delivery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import pandas.util.Strings;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
public class SearchController {
    private static final int DEFAULT_ROWS = 20;
    private static final String FL_FIELDS = "id,url,deliveryUrl,date,decade,year,site,host,title,searchCategory";
    private static final String EDISMAX_QF = "id^100.0 host^8 urlText^6.0 title^10.0 linkText1^2.5 linkText2^2.0 linkText3^1.0 linkText4^0.5 h1^1.0 metadata^0.5 fulltext^0.2";
    private static final String EDISMAX_PF = "id^100.0 host^10 urlText^8.0 title^20.0 linkText1^5.0 linkText2^4.0 linkText3^2.0 linkText4^1.0 h1^1.5 metadata^1 fulltext^1";
    private static final DateTimeFormatter ARCHIVE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH).withZone(ZoneOffset.UTC);
    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final WebClient webClient;

    public SearchController(@Value("${SOLR_SELECT_URL:http://wa-solr-prd-1.nla.gov.au:10017/solr/webarchive/select}") String solrSelectUrl) {
        log.info("Solr select URL: {}", solrSelectUrl);
        this.webClient = WebClient.builder().baseUrl(solrSelectUrl).build();
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "q", required = false) String query,
                         @RequestParam(name = "yearFrom", required = false) Integer yearFrom,
                         @RequestParam(name = "yearTo", required = false) Integer yearTo,
                         @RequestParam(name = "site", required = false) String site,
                         @RequestParam(name = "deliveryUrl", required = false) String deliveryUrl,
                         @RequestParam(name = "page", required = false) Integer page,
                         Model model) {
        query = Strings.emptyToNull(query);
        site = Strings.emptyToNull(site);
        deliveryUrl = Strings.emptyToNull(deliveryUrl);
        String extractedSite = extractSiteFilter(query);
        final String searchQuery = query;
        final String siteFilter = site != null ? site : extractedSite;
        final String deliveryUrlFilter = deliveryUrl;
        model.addAttribute("q", query);
        model.addAttribute("selectedYearFrom", yearFrom);
        model.addAttribute("selectedYearTo", yearTo);
        model.addAttribute("siteFilter", siteFilter);
        model.addAttribute("deliveryUrlFilter", deliveryUrlFilter);
        int currentPage = page == null || page < 1 ? 1 : page;
        model.addAttribute("page", currentPage);
        model.addAttribute("prefix", "");
        if (query == null) {
            model.addAttribute("numFound", 0L);
            model.addAttribute("groups", List.of());
            model.addAttribute("highlights", Map.of());
            model.addAttribute("totalPages", 1);
            return "Search";
        }

        try {
            var searchFuture = CompletableFuture.supplyAsync(() -> fetchSearchResults(searchQuery, yearFrom, yearTo, siteFilter, deliveryUrlFilter, currentPage));
            var yearCountsFuture = CompletableFuture.supplyAsync(() -> fetchYearCounts(searchQuery, siteFilter, deliveryUrlFilter));
            var rangeFuture = CompletableFuture.supplyAsync(() -> fetchDeliveryUrlDateRanges(searchQuery, yearFrom, yearTo, siteFilter, deliveryUrlFilter, currentPage));
            SolrResponse response = searchFuture.join();
            List<YearCount> yearCounts = yearCountsFuture.join();
            Map<String, String> deliveryUrlRanges = rangeFuture.join();
            log.info("deliveryUrlRanges keys: {}", deliveryUrlRanges.keySet());
            if (response == null) {
                model.addAttribute("numFound", 0L);
                model.addAttribute("groups", List.of());
                model.addAttribute("highlights", Map.of());
                model.addAttribute("yearCounts", yearCounts);
                model.addAttribute("yearMaxCount", 0L);
                model.addAttribute("yearScaleMax", 0.0);
                model.addAttribute("totalPages", 1);
                model.addAttribute("deliveryUrlRanges", Map.of());
            } else {
                String groupField = siteFilter == null ? "site" : "deliveryUrl";
                boolean groupedEnabled = deliveryUrlFilter == null;
                var grouped = groupedEnabled && response.grouped != null ? response.grouped.get(groupField) : null;
                Map<String, List<String>> highlights = new HashMap<>();
                if (response.highlighting != null) {
                    for (var entry : response.highlighting.entrySet()) {
                        var fulltext = entry.getValue().get("fulltext");
                        if (fulltext != null) {
                            highlights.put(entry.getKey(), fulltext);
                        }
                    }
                }
                List<SolrGroup> groups = groupedEnabled
                        ? (grouped == null || grouped.groups == null ? Collections.emptyList() : grouped.groups)
                        : toUngrouped(response);
                for (var group : groups) {
                    if (group.doclist == null || group.doclist.docs == null) continue;
                    processDocs(group.doclist.docs);
                }
                long numFound = groupedEnabled
                        ? (grouped == null ? 0L : grouped.matches)
                        : (response.response == null ? 0L : response.response.numFound);
                int totalPages = numFound == 0 ? 1 : (int) Math.ceil(numFound / (double) DEFAULT_ROWS);
                model.addAttribute("numFound", numFound);
                model.addAttribute("groups", groups);
                model.addAttribute("highlights", highlights);
                model.addAttribute("yearCounts", yearCounts);
                model.addAttribute("yearMaxCount", yearCounts.stream().mapToLong(YearCount::count).max().orElse(0L));
                model.addAttribute("yearScaleMax", yearCounts.stream()
                        .mapToDouble(yearCount -> Math.sqrt(yearCount.count()))
                        .max()
                        .orElse(0.0));
                model.addAttribute("totalPages", totalPages);
                model.addAttribute("deliveryUrlRanges", deliveryUrlRanges);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Search backend unavailable.");
            model.addAttribute("numFound", 0L);
            model.addAttribute("groups", List.of());
            model.addAttribute("highlights", Map.of());
            model.addAttribute("yearCounts", List.of());
            model.addAttribute("yearMaxCount", 0L);
            model.addAttribute("yearScaleMax", 0.0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("deliveryUrlRanges", Map.of());
        }
        return "Search";
    }

    public static class SolrResponse {
        public SolrResult response;
        public Map<String, SolrGroupedResult> grouped;
        public Map<String, Map<String, List<String>>> highlighting;
        public SolrResponseHeader responseHeader;
    }

    public static class SolrResult {
        public long numFound;
        public List<Map<String, Object>> docs;
    }

    public static class SolrResponseHeader {
        public Integer QTime;
    }

    public static class SolrGroupedResult {
        public long matches;
        public List<SolrGroup> groups;
    }

    public static class SolrGroup {
        public String groupValue;
        public SolrResult doclist;
    }

    private static String toArchiveDate(Object dateValue) {
        if (dateValue == null) return null;
        if (dateValue instanceof String dateString) {
            try {
                return ARCHIVE_DATE_FORMATTER.format(Instant.parse(dateString));
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private static String toDisplayDate(Object dateValue) {
        if (dateValue == null) return null;
        if (dateValue instanceof String dateString) {
            try {
                return DISPLAY_DATE_FORMATTER.format(Instant.parse(dateString));
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value;
        return value.substring(0, maxLength - 3) + "...";
    }

    private static void processDocs(List<Map<String, Object>> docs) {
        for (var doc : docs) {
            var archiveDate = toArchiveDate(doc.get("date"));
            if (archiveDate != null) {
                doc.put("archiveDate", archiveDate);
            }
            var displayDate = toDisplayDate(doc.get("date"));
            if (displayDate != null) {
                doc.put("displayDate", displayDate);
            }
            var titleValue = doc.get("title");
            var urlValue = doc.get("deliveryUrl") != null ? doc.get("deliveryUrl") : doc.get("url");
            if (titleValue instanceof String titleString) {
                doc.put("displayTitle", truncate(titleString, 300));
            } else if (urlValue instanceof String urlString) {
                doc.put("displayTitle", truncate(urlString, 300));
            }
            if (urlValue instanceof String urlString) {
                doc.put("displayUrl", truncate(urlString, 300));
            }
        }
    }

    private Map<String, String> fetchDeliveryUrlDateRanges(String query, Integer yearFrom, Integer yearTo, String site, String deliveryUrl, int page) {
        if (site == null || deliveryUrl != null) return Map.of();
        var ascFuture = CompletableFuture.supplyAsync(() -> fetchDeliveryUrlDates(query, yearFrom, yearTo, site, page, "date asc"));
        var descFuture = CompletableFuture.supplyAsync(() -> fetchDeliveryUrlDates(query, yearFrom, yearTo, site, page, "date desc"));
        Map<String, String> earliest = ascFuture.join();
        Map<String, String> latest = descFuture.join();
        Map<String, String> ranges = new HashMap<>();
        for (var entry : earliest.entrySet()) {
            String url = entry.getKey();
            String start = entry.getValue();
            String end = latest.get(url);
            ranges.put(url, formatDateRange(start, end));
        }
        return ranges;
    }

    private Map<String, String> fetchDeliveryUrlDates(String query, Integer yearFrom, Integer yearTo, String site, int page, String sort) {
        final String[] urlHolder = new String[1];
        SolrResponse response = webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .queryParam("q", query)
                            .queryParam("defType", "edismax")
                            .queryParam("qf", EDISMAX_QF)
                            .queryParam("pf", EDISMAX_PF)
                            .queryParam("ps", "2")
                            .queryParam("mm", "2<75%")
                            .queryParam("tie", "0.1")
                            .queryParam("rows", DEFAULT_ROWS)
                            .queryParam("fl", "deliveryUrl,date")
                            .queryParam("group", "true")
                            .queryParam("group.field", "deliveryUrl")
                            .queryParam("group.limit", 1)
                            .queryParam("group.sort", sort)
                            .queryParam("group.offset", (page - 1) * DEFAULT_ROWS)
                            .queryParam("wt", "json");
                    if (yearFrom != null || yearTo != null) {
                        String rangeStart = yearFrom == null ? "*" : yearFrom.toString();
                        String rangeEnd = yearTo == null ? "*" : yearTo.toString();
                        builder = builder.queryParam("fq", "year:[" + rangeStart + " TO " + rangeEnd + "]");
                    }
                    if (site != null) {
                        builder = builder.queryParam("fq", "site:" + site);
                    }
                    var uri = builder.build();
                    urlHolder[0] = uri.toString();
                    return uri;
                })
                .retrieve()
                .bodyToMono(SolrResponse.class)
                .block();
        Integer qtime = response != null && response.responseHeader != null ? response.responseHeader.QTime : null;
        log.info("Solr deliveryUrl range QTime_ms: {} URL: {}", qtime, urlHolder[0]);
        Map<String, String> dates = new HashMap<>();
        var grouped = response == null || response.grouped == null ? null : response.grouped.get("deliveryUrl");
        if (grouped == null || grouped.groups == null) return dates;
        for (var group : grouped.groups) {
            if (group.doclist == null || group.doclist.docs == null || group.doclist.docs.isEmpty()) continue;
            var doc = group.doclist.docs.get(0);
            var deliveryUrl = doc.get("deliveryUrl");
            if (deliveryUrl == null) {
                deliveryUrl = group.groupValue;
            }
            var dateValue = doc.get("date");
            if (deliveryUrl instanceof String url && dateValue instanceof String dateString) {
                String displayDate = toDisplayDate(dateString);
                if (displayDate != null) {
                    dates.put(url, displayDate);
                }
            }
        }
        return dates;
    }

    private static String formatDateRange(String start, String end) {
        if (start == null && end == null) return null;
        if (start == null) return end;
        if (end == null) return start;
        if (start.equals(end)) return start;
        return start + " — " + end;
    }

    private static String escapeSolrPhrase(String value) {
        if (value == null) return null;
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String extractSiteFilter(String query) {
        if (query == null) return null;
        var tokens = query.split("\\s+");
        for (var token : tokens) {
            if (token.startsWith("site:") && token.length() > 5) {
                return token.substring(5);
            }
        }
        return null;
    }

    private static List<SolrGroup> toUngrouped(SolrResponse response) {
        if (response == null || response.response == null || response.response.docs == null) return List.of();
        SolrGroup group = new SolrGroup();
        SolrResult result = new SolrResult();
        result.docs = response.response.docs;
        result.numFound = response.response.numFound;
        group.doclist = result;
        group.groupValue = null;
        return List.of(group);
    }


    private List<YearCount> fetchYearCounts(String query, String site, String deliveryUrl) {
        try {
            final String[] urlHolder = new String[1];
            SolrResponse response = webClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .queryParam("q", query)
                                .queryParam("defType", "edismax")
                                .queryParam("qf", EDISMAX_QF)
                                .queryParam("pf", EDISMAX_PF)
                                .queryParam("ps", "2")
                                .queryParam("mm", "2<75%")
                                .queryParam("tie", "0.1")
                                .queryParam("rows", 100)
                                .queryParam("fl", "id")
                                .queryParam("group", "true")
                                .queryParam("group.field", "year")
                                .queryParam("sort", "year asc")
                                .queryParam("wt", "json");
                        if (site != null) {
                            builder = builder.queryParam("fq", "site:" + site);
                        }
                        if (deliveryUrl != null) {
                            builder = builder.queryParam("fq", "deliveryUrl:\"" + escapeSolrPhrase(deliveryUrl) + "\"");
                        }
                        var uri = builder.build();
                        urlHolder[0] = uri.toString();
                        return uri;
                    })
                    .retrieve()
                    .bodyToMono(SolrResponse.class)
                    .block();
            Integer qtime = response != null && response.responseHeader != null ? response.responseHeader.QTime : null;
            log.info("Solr year counts QTime_ms: {} URL: {}", qtime, urlHolder[0]);
            var grouped = response == null || response.grouped == null ? null : response.grouped.get("year");
            if (grouped == null || grouped.groups == null) return List.of();
            return grouped.groups.stream()
                    .map(group -> new YearCount(parseYear(group.groupValue), group.doclist == null ? 0L : group.doclist.numFound))
                    .filter(yearCount -> yearCount.year() != null)
                    .toList();
        } catch (Exception e) {
            log.warn("Unable to fetch year counts", e);
            return List.of();
        }
    }

    private SolrResponse fetchSearchResults(String query, Integer yearFrom, Integer yearTo, String site, String deliveryUrl, int page) {
        final String[] urlHolder = new String[1];
        SolrResponse response = webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .queryParam("q", query)
                            .queryParam("defType", "edismax")
                            .queryParam("qf", EDISMAX_QF)
                            .queryParam("pf", EDISMAX_PF)
                            .queryParam("ps", "2")
                            .queryParam("mm", "2<75%")
                            .queryParam("tie", "0.1")
                            .queryParam("rows", DEFAULT_ROWS)
                            .queryParam("sort", "score desc, date asc")
                            .queryParam("fl", FL_FIELDS)
                            .queryParam("hl", "true")
                            .queryParam("hl.fl", "fulltext")
                            .queryParam("hl.fragsize", "400")
                            .queryParam("hl.snippets", "1")
                            .queryParam("hl.simple.pre", "<strong>")
                            .queryParam("hl.simple.post", "</strong>")
                            .queryParam("wt", "json");
                    if (deliveryUrl != null) {
                        builder = builder.queryParam("start", (page - 1) * DEFAULT_ROWS);
                    } else if (site == null) {
                        builder = builder
                                .queryParam("group", "true")
                                .queryParam("group.field", "site")
                                .queryParam("group.limit", 1)
                                .queryParam("group.offset", (page - 1) * DEFAULT_ROWS);
                    } else if (deliveryUrl == null) {
                        builder = builder
                                .queryParam("group", "true")
                                .queryParam("group.field", "deliveryUrl")
                                .queryParam("group.limit", 1)
                                .queryParam("group.sort", "date asc")
                                .queryParam("group.offset", (page - 1) * DEFAULT_ROWS);
                    }
                    if (yearFrom != null || yearTo != null) {
                        String rangeStart = yearFrom == null ? "*" : yearFrom.toString();
                        String rangeEnd = yearTo == null ? "*" : yearTo.toString();
                        builder = builder.queryParam("fq", "year:[" + rangeStart + " TO " + rangeEnd + "]");
                    }
                    if (site != null) {
                        builder = builder.queryParam("fq", "site:" + site);
                    }
                    if (deliveryUrl != null) {
                        builder = builder.queryParam("fq", "deliveryUrl:\"" + escapeSolrPhrase(deliveryUrl) + "\"");
                    }
                    var uri = builder.build();
                    urlHolder[0] = uri.toString();
                    return uri;
                })
                .retrieve()
                .bodyToMono(SolrResponse.class)
                .block();
        Integer qtime = response != null && response.responseHeader != null ? response.responseHeader.QTime : null;
        log.info("Solr query QTime_ms: {} URL: {}", qtime, urlHolder[0]);
        return response;
    }

    private static Integer parseYear(Object value) {
        if (value instanceof Integer year) return year;
        if (value instanceof Long year) return year.intValue();
        if (value instanceof String year) {
            try {
                return Integer.parseInt(year);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    public record YearCount(Integer year, long count) {
    }
}
