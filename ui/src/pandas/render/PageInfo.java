package pandas.render;

import org.jsoup.nodes.Document;
import org.jsoup.parser.StreamParser;

public class PageInfo {
    private static final int MAX_TITLE_LEN = 1000;
    private static final int MAX_TEXT_LEN = 3000;

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

    public PageInfo(int status, String reason, String contentType, Document document) {
        this.status = status;
        this.reason = reason;
        this.contentType = contentType;
        charset = document.charset().name();
        String title = null;
        if (document.location().startsWith("https://bsky.app/profile/")) {
            var ogTitle = document.selectFirst("meta[property=og:title]");
            if (ogTitle != null) {
                title = ogTitle.attr("content") + " on Bluesky";
            }
        }
        if (title == null) title = cleanTitle(document.title());
        this.title = title;
        location = null;
        var description = document.selectFirst("meta[property=description]");
        text = (description != null ? description.html() : "") + document.text();
    }

    private static String cleanTitle(String title) {
        title = title.replaceAll("\\s\\s+", " ").trim();
        if (title.length() > MAX_TITLE_LEN) {
            title = title.substring(0, MAX_TITLE_LEN) + "...";
        }
        return title;
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
