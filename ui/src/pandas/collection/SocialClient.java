package pandas.collection;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import pandas.core.Config;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class SocialClient {
    private final String baseUrl;

    public SocialClient(Config config) {
        this.baseUrl = config.getSocialUrl();
    }

    private static Pattern MATCHES_RE = Pattern.compile("Found ([0-9,]+) matching");
    public Results search(String query, String sort) throws IOException {
        if (baseUrl == null) throw new IllegalStateException("PANDAS_SOCIAL_URL not configured");
        String url = baseUrl + "/search?q=" + UriUtils.encodeQueryParam(query, UTF_8) +
                     "&sort=" + UriUtils.encodeQueryParam(sort, UTF_8);
        var resultsDocument = Jsoup.connect(url).get();

        // extract the number from "<p>Found 123 matching posts...</p>"
        Long totalHits = null;
        var matchesParagraph = resultsDocument.selectFirst("p");
        if (matchesParagraph != null) {
            var matcher = MATCHES_RE.matcher(matchesParagraph.text());
            if (matcher.find()) {
                totalHits = Long.parseLong(matcher.group(1).replace(",", ""));
            }
        }

        var posts = resultsDocument.select(".post")
                .stream().map(post -> post.outerHtml()).toList();
        return new Results(query, totalHits, posts);
    }

    public record Results (String query, Long totalHits, List<String> posts) {
    }
}
