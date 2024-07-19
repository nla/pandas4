package pandas.render;

import org.attoparser.AbstractMarkupHandler;
import org.attoparser.ParseException;
import org.attoparser.util.TextUtil;
import org.jsoup.parser.Parser;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class PageInfo {
    private final int status;
    private final String reason;
    private final String contentType;
    private final String charset;
    private final String title;
    private final String location;
    private final String text;

    public PageInfo(int status, String reason, String contentType, String charset, String title, String location, String text) {
        this.status = status;
        this.reason = reason;
        this.contentType = contentType;
        this.charset = charset;
        this.title = title;
        this.location = location;
        this.text = text;
    }

    public int weight() {
        int weight = 20;
        if (reason != null) weight += reason.length();
        if (contentType != null) weight += contentType.length();
        if (charset != null) weight += charset.length();
        if (title != null) weight += title.length();
        if (location != null) weight += location.length();
        if (text != null) weight += text.length();
        return weight;
    }

    public String getLocation() {
        return location;
    }

    public String getText() {
        return text;
    }

    static class TitleHandler extends AbstractMarkupHandler {
        private static final Pattern WHITESPACE = Pattern.compile("\\s+");
        private static final Set BLOCK_TAGS = Set.of(
                "html", "head", "body", "frameset", "style", "meta", "link", "title", "frame",
                "noframes", "section", "nav", "aside", "hgroup", "header", "footer", "p", "h1", "h2", "h3", "h4", "h5", "h6",
                "ul", "ol", "pre", "div", "blockquote", "hr", "address", "figure", "figcaption", "form", "fieldset", "ins",
                "del", "dl", "dt", "dd", "li", "table", "caption", "thead", "tfoot", "tbody", "colgroup", "col", "tr", "th",
                "td", "video", "audio", "canvas", "details", "menu", "plaintext", "template", "article", "main",
                "svg", "math", "center", "dir", "applet", "marquee", "listing");

        final char[] TITLE = "title".toCharArray();
        final int maxTitleLen = 1000;
        final int maxTextLen = 3000;
        boolean withinTitle = false;
        String title;
        final StringBuilder textBuffer = new StringBuilder();

        @Override
        public void handleCloseElementStart(char[] buffer, int nameOffset, int nameLen, int line, int col) throws ParseException {
            withinTitle = false;
            addSpaceIfBlockTag(buffer, nameOffset, nameLen);
        }

        @Override
        public void handleOpenElementEnd(char[] buffer, int nameOffset, int nameLen, int line, int col) throws ParseException {
            withinTitle = TextUtil.equals(false, buffer, nameOffset, nameLen, TITLE, 0, TITLE.length);
            addSpaceIfBlockTag(buffer, nameOffset, nameLen);
        }

        private void addSpaceIfBlockTag(char[] buffer, int nameOffset, int nameLen) {
            String tag = new String(buffer, nameOffset, nameLen).toLowerCase(Locale.ROOT);
            if (BLOCK_TAGS.contains(tag)) {
                if (!endsWithWhitespace(textBuffer)) {
                    textBuffer.append(" ");
                }
            }
        }

        @Override
        public void handleText(char[] buffer, int offset, int len, int line, int col) throws ParseException {
            if (withinTitle && title == null) {
                title = decodeText(buffer, offset, Math.min(len, maxTitleLen));
            }
            int remaining = maxTextLen - textBuffer.length();
            if (remaining > 0) {
                String text = decodeText(buffer, offset, Math.min(len, remaining));
                if (startsWithWhitespace(text) && (textBuffer.isEmpty() || endsWithWhitespace(textBuffer))) {
                    textBuffer.append(text, 1, text.length());
                } else {
                    textBuffer.append(text);
                }
            }

        }

        private static boolean startsWithWhitespace(CharSequence s) {
            return !s.isEmpty() && Character.isWhitespace(s.charAt(0));
        }

        private static boolean endsWithWhitespace(CharSequence s) {
            return !s.isEmpty() && Character.isWhitespace(s.charAt(s.length() - 1));
        }

        private String decodeText(char[] buffer, int offset, int len) {
            String text = Parser.unescapeEntities(new String(buffer, offset, len), false);
            text = WHITESPACE.matcher(text).replaceAll(" ");
            return text;
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
