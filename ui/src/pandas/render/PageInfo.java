package pandas.render;

import org.attoparser.AbstractMarkupHandler;
import org.attoparser.ParseException;
import org.attoparser.util.TextUtil;
import org.jsoup.parser.Parser;

public class PageInfo {
    private final int status;
    private final String reason;
    private final String contentType;
    private final String charset;
    private final String title;
    private final String location;

    public PageInfo(int status, String reason, String contentType, String charset, String title, String location) {
        this.status = status;
        this.reason = reason;
        this.contentType = contentType;
        this.charset = charset;
        this.title = title;
        this.location = location;
    }

    public String getLocation() {
        return location;
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
                title = Parser.unescapeEntities(new String(buffer, offset, Math.min(len, maxLen)), false);
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
