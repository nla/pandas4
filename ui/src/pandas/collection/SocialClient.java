package pandas.collection;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import pandas.core.Config;
import pandas.social.SocialJson;
import pandas.social.SocialResults;

import java.io.IOException;
import java.net.URI;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class SocialClient {
    private final String baseUrl;

    public SocialClient(Config config) {
        this.baseUrl = config.getSocialUrl() == null ? null :
                config.getSocialUrl().replaceFirst("/+$", "");
    }

    public SocialResults search(String query, String sort) throws IOException {
        if (baseUrl == null) throw new IllegalStateException("PANDAS_SOCIAL_URL not configured");
        String url = baseUrl + "/search?q=" + UriUtils.encodeQueryParam(query, UTF_8) +
                "&sort=" + UriUtils.encodeQueryParam(sort, UTF_8);
        return SocialJson.mapper.readValue(URI.create(url).toURL(), SocialResults.class);
    }
}
