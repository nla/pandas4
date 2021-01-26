package pandas.render;

import org.attoparser.AbstractMarkupHandler;
import org.attoparser.MarkupParser;
import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;
import org.attoparser.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

public class PageInfo {
    private static final Logger log = LoggerFactory.getLogger(PageInfo.class);

    private final int status;
    private final String reason;
    private final String contentType;
    private final String charset;
    private final String title;

    public PageInfo(int status, String reason, String contentType, String charset, String title) {
        this.status = status;
        this.reason = reason;
        this.contentType = contentType;
        this.charset = charset;
        this.title = title;
    }

    public static PageInfo fetch(String url) throws IOException {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("bad url");
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            int status = connection.getResponseCode();
            String reason = connection.getResponseMessage();
            String contentType = connection.getHeaderField("Content-Type");
            MediaType mediaType = MediaType.parseMediaType(contentType);
            String charsetName = mediaType.getParameter("charset");
            String title = null;
            if (mediaType.equalsTypeAndSubtype(MediaType.TEXT_HTML)) {
                TitleHandler handler = new TitleHandler();

                InputStream stream = connection.getInputStream();
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
            return new PageInfo(status, reason, contentType, charsetName, title);
        } finally {
            connection.getInputStream().close();
        }
    }

    static class TitleHandler extends AbstractMarkupHandler {
        final char[] TITLE = "title".toCharArray();
        final int maxLen = 1000;
        boolean withinTitle = false;
        String title;

        @Override
        public void handleCloseElementStart(char[] buffer, int nameOffset, int nameLen, int line, int col) throws ParseException {
            withinTitle = false;
        }

        @Override
        public void handleOpenElementEnd(char[] buffer, int nameOffset, int nameLen, int line, int col) throws ParseException {
            withinTitle = TextUtil.equals(false, buffer, nameOffset, nameLen, TITLE, 0, TITLE.length);
        }

        @Override
        public void handleText(char[] buffer, int offset, int len, int line, int col) throws ParseException {
            if (withinTitle && title == null) {
                title = new String(buffer, offset, Math.min(len, maxLen));
            }
        }
    }

    public int getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getContentType() {
        return contentType;
    }

    public String getCharset() {
        return charset;
    }

    public String getTitle() {
        return title;
    }
}
