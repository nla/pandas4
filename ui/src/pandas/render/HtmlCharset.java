package pandas.render;

import org.apache.commons.io.input.BoundedInputStream;
import org.attoparser.AbstractMarkupHandler;
import org.attoparser.MarkupParser;
import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;
import org.attoparser.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HtmlCharset {
    private static final Logger log = LoggerFactory.getLogger(HtmlCharset.class);

    /**
     * Probes the start of a stream for HTML meta charset tag. Uses mark() and reset() so as not to consume the actual
     * stream content. Returns null if no meta charset tag was found or if we encountered an unrecoverable HTML parse
     * error.
     */
    public static String detect(BufferedInputStream stream) throws IOException {
        int limit = 4096;
        stream.mark(limit);
        HtmlCharset.Handler handler = new HtmlCharset.Handler();
        BoundedInputStream bounded = new BoundedInputStream(stream, limit);
        bounded.setPropagateClose(false);
        try {
            new MarkupParser(ParseConfiguration.htmlConfiguration()).parse(new InputStreamReader(bounded, StandardCharsets.ISO_8859_1), handler);
        } catch (ParseException e) {
            log.warn("charset detection error", e);
            // ignore
        }
        stream.reset();
        return handler.charset;
    }

    static class Handler extends AbstractMarkupHandler {
        private static final char[] META = "meta".toCharArray();
        private static final char[] CHARSET = "charset".toCharArray();
        private static final char[] CONTENT = "content".toCharArray();
        private static final char[] HTTP_EQUIV = "http-equiv".toCharArray();
        private static final char[] CONTENT_TYPE = "content-type".toCharArray();

        boolean withinMeta = false;
        boolean httpEquivContentType = false;
        String charset;
        String content;

        @Override
        public void handleStandaloneElementStart(char[] buffer, int nameOffset, int nameLen, boolean minimized, int line, int col) throws ParseException {
            withinMeta = TextUtil.equals(false, buffer, nameOffset, nameLen, META, 0, META.length);
            content = null;
            httpEquivContentType = false;
        }

        @Override
        public void handleStandaloneElementEnd(char[] buffer, int nameOffset, int nameLen, boolean minimized, int line, int col) throws ParseException {
            if (withinMeta && httpEquivContentType) {
                try {
                    charset = MediaType.parseMediaType(content).getParameter("charset");
                } catch (InvalidMediaTypeException e) {
                    charset = null;
                }
            }
        }

        @Override
        public void handleAttribute(char[] buffer, int nameOffset, int nameLen, int nameLine, int nameCol, int operatorOffset, int operatorLen, int operatorLine, int operatorCol, int valueContentOffset, int valueContentLen, int valueOuterOffset, int valueOuterLen, int valueLine, int valueCol) throws ParseException {
            if (withinMeta && TextUtil.equals(false, buffer, nameOffset, nameLen, CHARSET, 0, CHARSET.length)) {
                charset = new String(buffer, valueContentOffset, valueContentLen);
            } else if (withinMeta && TextUtil.equals(false, buffer, nameOffset, nameLen, CONTENT, 0, CONTENT.length)){
                content = new String(buffer, valueContentOffset, valueContentLen);
            } else if (withinMeta && TextUtil.equals(false, buffer, nameOffset, nameLen, HTTP_EQUIV, 0, HTTP_EQUIV.length)
                    && TextUtil.equals(false, buffer, valueContentOffset, valueContentLen, CONTENT_TYPE, 0, CONTENT_TYPE.length)) {
                httpEquivContentType = true;
            }
        }
    }

}
