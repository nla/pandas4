package pandas.render;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.attoparser.MarkupParser;
import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

@Controller
public class PageInfoController {
    private static final Logger log = LoggerFactory.getLogger(PageInfo.class);

    private final OkHttpClient httpClient;

    public PageInfoController(OkHttpClient httpClient) {
        this.httpClient = httpClient.newBuilder().followRedirects(false).followSslRedirects(false).build();
    }

    @GetMapping(value = "/pageinfo", produces = "application/json")
    @ResponseBody
    public PageInfo load(@RequestParam(name = "url") String url) throws Exception {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("bad url");
        }

        try (Response response = httpClient.newCall(new Request.Builder().url(url).build()).execute()) {
            String contentType = response.header("Content-Type", "application/octet-stream");
            MediaType mediaType = MediaType.parseMediaType(contentType);
            String charsetName = mediaType.getParameter("charset");
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
                if (charsetName == null) {
                    BufferedInputStream bis = new BufferedInputStream(stream);
                    charsetName = HtmlCharset.detect(bis);
                    stream = bis;
                }

                Charset charset = StandardCharsets.ISO_8859_1;
                if (charsetName != null) {
                    try {
                        charset = Charset.forName(charsetName);
                    } catch (UnsupportedCharsetException e) {
                        log.warn("Unsupported charset {}, defaulting to iso-8859-1", charsetName);
                    }
                }

                try {
                    new MarkupParser(ParseConfiguration.htmlConfiguration()).parse(new InputStreamReader(stream, charset), handler);
                } catch (ParseException e) {
                    log.warn("Exception parsing " + url, e);
                }
                title = handler.title;
            }
            return new PageInfo(response.code(), reason, contentType, charsetName, title, response.header("Location"));
        }
    }
}
