package pandas.collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import pandas.core.Config;
import pandas.util.DateFormats;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Controller
public class PagesController {
    private static final Logger log = LoggerFactory.getLogger(PagesController.class);
    private final Config config;
    private final CaptureIndex captureIndex;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PagesController(Config config, CaptureIndex captureIndex) {
        this.config = config;
        this.captureIndex = captureIndex;
    }

    @GetMapping("/pages")
    public String search(@RequestParam(value = "url", required = false) String url, Model model) {
        if (url != null && url.endsWith("*")) return searchGrouped(url, model);
        List<Capture> captures = url == null || url.isBlank() ? List.of() : captureIndex.queryDateDesc(url);
        String queryString = captures.stream()
                .map(Capture::getFile)
                .distinct()
                .map(filename -> "filename=" + UriUtils.encodeQueryParam(filename, UTF_8))
                .collect(Collectors.joining("&"));
        String queryUrl = config.getBambooUrl() + "/api/v2/CrawlsByWarcFilename";
        Map<String, CrawlsByFilename> crawlsByFilenameMap = new HashMap<>();
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(queryUrl).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
            connection.getOutputStream().write(queryString.getBytes(UTF_8));
            var result = objectMapper.readValue(connection.getInputStream(), CrawlsByFilename[].class);
            for (CrawlsByFilename crawlsByFilename : result) {
                crawlsByFilenameMap.put(crawlsByFilename.filename(), crawlsByFilename);
            }
        } catch (IOException e) {
            log.error("Query failed: POST " + queryUrl, e);
        }
        model.addAttribute("captures", captures);
        model.addAttribute("crawlsByFilenameMap", crawlsByFilenameMap);
        model.addAttribute("url", url);
        model.addAttribute("dateTimeFormat", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault()));
        return "PageSearch";
    }

    private String searchGrouped(String url, Model model) {
        model.addAttribute("groups", captureIndex.queryGrouped(url));
        model.addAttribute("url", url);
        model.addAttribute("dateTimeFormat", DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault()));
        return "PageSearchGrouped";
    }

    record CrawlsByFilename(long warcId, String filename, long crawlId, String crawlName, Long pandasInstanceId) {
    }

    @GetMapping("/replay")
    public String replay(@RequestParam String url, @RequestParam Instant date, Model model) {
        model.addAttribute("url", url);
        model.addAttribute("arcdate", DateFormats.ARC_DATE.format(date));
        return "PageView";
    }
}
