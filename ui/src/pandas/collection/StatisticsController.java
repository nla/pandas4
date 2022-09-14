package pandas.collection;

import org.apache.commons.io.FileUtils;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import java.time.LocalDate;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Controller
public class StatisticsController {
    private final JdbcTemplate db;

    public StatisticsController(JdbcTemplate db) {
        this.db = db;
    }

    @GetMapping("/statistics")
    public String list(Model model) {
        model.addAttribute("contentTypeRows", db.query("""
                select content_type, sum(snapshots) as snapshots, sum(storage) as storage
                from (select case
                                 when content_type like 'image/%' or content_type like 'img/%' then 'Images'
                                 when content_type in ('text/html', 'application/xhtml+xml') then 'HTML'
                                 when content_type like 'audio/%' then 'Audio'
                                 when content_type like 'video/%' then 'Video'
                                 when content_type like 'font/%'
                                     or content_type like 'x-font/%'
                                     or content_type in ('application/x-font-woff',
                                                         'application/font-woff',
                                                         'application/vnd.ms-fontobject',
                                                         'application/font-sfnt') then 'Fonts'
                                 when content_type like 'text/css' then 'Stylesheets'
                                 when content_type in ('application/json',
                                                       'application/xml',
                                                       'text/csv',
                                                       'application/json+oembed') then 'Data files (JSON/XML/CSV)'
                                 when content_type in ('application/x-javascript', 'text/javascript') then 'JavaScript'
                                 when content_type in ('application/rss+xml', 'application/atom+xml') then 'RSS/Atom feeds'
                                 when content_type in ('application/pdf',
                                                       'application/postscript',
                                                       'application/epub+zip',
                                                       'application/epub') then 'Print documents (PDF/PS/EPUB)'
                                 when content_type in ('text/plain',
                                                       'application/msword',
                                                       'application/vnd.ms-word.document.12',
                                                       'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                                                       'application/rtf') then 'Office documents'
                                 when content_type in ('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                                                       'application/x-msexcel',
                                                       'application/vnd.ms-excel',
                                                       'application/vnd.oasis.opendocument.spreadsheet') then 'Spreadsheets'
                                 else 'Other' end as content_type,
                             snapshots,
                             storage
                      from type_stats) t
                group by content_type
                order by sum(snapshots) desc
                """, new DataClassRowMapper<>(ContentTypeResult.class)));

        model.addAttribute("summary", db.queryForObject(
            "select sum(snapshots) as snapshots, sum(storage) as storage from type_stats", new DataClassRowMapper<>(Summary.class)));
        return "StatisticsList";
    }

    public record Summary(long snapshots, long storage) {
    }

    @GetMapping("/statistics/url-snapshots-by-content-type")
    public String urlSnapshotsByContentType(@RequestParam(required = false) Integer year, Model model) {
        List<ContentTypeResult> rows;
        if (year != null) {
            rows = db.query("""
                select content_type,
                    sum(snapshots) as snapshots,
                    sum(storage) as storage
                from type_stats
                where year = ?
                group by content_type
                order by snapshots desc
                """, new DataClassRowMapper<>(ContentTypeResult.class), year);
        } else {
            rows = db.query("""
                select content_type,
                    sum(snapshots) as snapshots,
                    sum(storage) as storage
                from type_stats
                group by content_type
                order by snapshots desc
                """, new DataClassRowMapper<>(ContentTypeResult.class));
        }
        String title = "URL Snapshots by Content Type";
        if (year != null) title += " (" + year + ")";
        model.addAttribute("title", title);
        model.addAttribute("columns", List.of("Content Type", "URL Snapshots", "Storage"));
        model.addAttribute("rows", rows);
        model.addAttribute("totalSnapshots", format(rows.stream().mapToLong(row -> row.snapshots()).sum()));
        model.addAttribute("totalStorage", FileUtils.byteCountToDisplaySize(rows.stream().mapToLong(row -> row.storage()).sum()));
        return "StatisticsView";
    }

    record ContentTypeResult(String contentType, long snapshots, long storage) {
        public List<Object> values() {
            return List.of(String.format("%,d", snapshots),
                    FileUtils.byteCountToDisplaySize(storage));
        }

        public String key() {
            return contentType;
        }

        public String link() {
            return "url-snapshots-by-year?contentType=" + UriUtils.encodeQueryParam(contentType, UTF_8);
        }
    }

    @GetMapping("/statistics/url-snapshots-by-year")
    public String urlSnapshotsByYear(@RequestParam(required = false) String contentType, Model model) {
        List<YearResult> rows;
        if (contentType != null) {
            rows = db.query("""
                select year,
                    sum(snapshots) as snapshots,
                    sum(storage) as storage
                from type_stats
                where year >= 1995 and year <= ? 
                  and content_type = ?
                group by year
                order by year
                """, new DataClassRowMapper<>(YearResult.class), LocalDate.now().getYear(), contentType);
        } else {
            rows = db.query("""
                select year,
                    sum(snapshots) as snapshots,
                    sum(storage) as storage
                from type_stats
                where year >= 1995 and year <= ? 
                group by year
                order by year
                """, new DataClassRowMapper<>(YearResult.class), LocalDate.now().getYear());
        }
        String title = "URL Snapshots by Year";
        if (contentType != null) title += " (" + contentType + ")";
        model.addAttribute("title", title);
        model.addAttribute("columns", List.of("Year", "URL Snapshots", "Storage"));
        model.addAttribute("rows", rows);
        model.addAttribute("chartLabels", rows.stream().map(YearResult::year).toList());
        model.addAttribute("chartData", rows.stream().map(YearResult::snapshots).toList());
        model.addAttribute("chartData2", rows.stream().map(row -> row.storage() / (1024.0 * 1024.0 * 1024.0)).toList());

        model.addAttribute("totalSnapshots", format(rows.stream().mapToLong(YearResult::snapshots).sum()));
        model.addAttribute("totalStorage", FileUtils.byteCountToDisplaySize(rows.stream().mapToLong(YearResult::storage).sum()));
        return "StatisticsView";
    }

    private static String format(long number) {
        return String.format("%,d", number);
    }

    record YearResult(short year, long snapshots, long storage) {
        public List<Object> values() {
            return List.of(format(snapshots),
                    FileUtils.byteCountToDisplaySize(storage));
        }

        public String key() {
            return String.valueOf(year);
        }

        public String link() {
            return "url-snapshots-by-content-type?year=" + year;
        }
    }

}
