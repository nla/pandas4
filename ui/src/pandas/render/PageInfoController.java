package pandas.render;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.attoparser.MarkupParser;
import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;

@Controller
public class PageInfoController {
    private static final Logger log = LoggerFactory.getLogger(PageInfo.class);

    private final OkHttpClient httpClient;
    private final LoadingCache<String, PageInfo> cache;

    public PageInfoController(OkHttpClient httpClient) {
        this.httpClient = httpClient.newBuilder().followRedirects(true).followSslRedirects(true).build();
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .weigher((String url, PageInfo pageInfo) -> 32 + url.length() + pageInfo.weight())
                .maximumWeight(20000000) // roughly 20 MB
                .build(new CacheLoader<>() {
                    @Override
                    public PageInfo load(String url) throws IOException {
                        return fetchPageInfo(url);
                    }
                });
    }

    @GetMapping(value = "/pageinfo", produces = "application/json")
    @ResponseBody
    public PageInfo load(@RequestParam(name = "url") String url) throws Exception {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("bad url");
        }
        return cache.get(url);
    }

    @NotNull
    private PageInfo fetchPageInfo(String url) throws IOException {
        try (Response response = httpClient.newCall(new Request.Builder().url(url)
                .header("User-Agent", "nla.gov.au_bot (National Library of Australia Legal Deposit Request; +https://www.nla.gov.au/legal-deposit/request)")
                .build()).execute()) {
            String contentType = response.header("Content-Type", "application/octet-stream");
            MediaType mediaType = MediaType.parseMediaType(contentType);
            Charset charset = mediaType.getCharset();
            String title = null;
            String reason = response.message();
            if (reason.isBlank()) {
                HttpStatus status = HttpStatus.resolve(response.code());
                reason = status == null ? null : status.getReasonPhrase();
            }
            okhttp3.ResponseBody body = response.body();
            if (mediaType.equalsTypeAndSubtype(MediaType.TEXT_HTML) && body != null) {
                PageInfo.TitleHandler handler = new PageInfo.TitleHandler();

                InputStream stream = body.byteStream();
                // if there was no charset in the Content-Type header, probe for meta tags near the top of the file
                if (charset == null) {
                    BufferedInputStream bis = new BufferedInputStream(stream);
                    String charsetName = HtmlCharset.detect(bis);
                    if (charsetName != null) {
                        try {
                            charset = Charset.forName(charsetName);
                        } catch (UnsupportedCharsetException e) {
                            log.warn("Unsupported charset {}, defaulting to iso-8859-1", charsetName);
                            charset = StandardCharsets.ISO_8859_1;
                        }
                    }
                    stream = bis;
                }

                try {
                    new MarkupParser(ParseConfiguration.htmlConfiguration()).parse(new InputStreamReader(stream, charset), handler);
                } catch (ParseException e) {
                    log.warn("Exception parsing " + url, e);
                }
                title = handler.title.replaceAll("\\s\\s+", " ").trim();
                if (title.length() > 1000) {
                    title = title.substring(0, 1000) + "...";
                }
            }
            String location = null;
            if (response.priorResponse() != null) {
                location = response.request().url().toString();
            }
            return new PageInfo(response.code(), reason, contentType, charset == null ? null : charset.name(), title, location);
        } catch (UnknownHostException e) {
            return new PageInfo(-1, e.getMessage(), null, null, null, null);
        }
    }
}
