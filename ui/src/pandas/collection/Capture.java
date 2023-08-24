package pandas.collection;

import org.springframework.http.HttpStatus;
import pandas.util.DateFormats;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Capture {

    private final String url;
    private final Instant date;
    private final String contentType;
    private final Integer status;
    private final String digest;
    private final String redirectUrl;
    private final Long length;
    private final long offset;
    private final String file;

    public Capture(String cdxLine) {
        String[] fields = cdxLine.split(" ");
        this.date = DateFormats.ARC_DATE.parse(fields[1], Instant::from);
        this.url = fields[2];
        this.contentType = fields[3];
        this.status = fields[4].equals("-") ? null : Integer.parseInt(fields[4]);
        this.digest = fields[5];
        this.redirectUrl = fields[6].equals("-") ? null : fields[6];
        this.length = fields[8].equals("-") ? null : Long.parseLong(fields[8]);
        this.offset = Long.parseLong(fields[9]);
        this.file = fields[10];
    }

    public String getUrl() {
        return url;
    }

    public Instant getDate() {
        return date;
    }

    public String getReplayUrl() {
        return "https://webarchive.nla.gov.au/awa/" + DateFormats.ARC_DATE.format(getDate()) + "/" + getUrl();
    }

    public String getContentType() {
        return contentType;
    }

    public Integer getStatus() {
        return status;
    }

    public String getStatusPhrase() {
        HttpStatus httpStatus = HttpStatus.resolve(getStatus());
        return httpStatus == null ? null : httpStatus.getReasonPhrase();
    }

    public String getDigest() {
        return digest;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public Long getLength() {
        return length;
    }

    public long getOffset() {
        return offset;
    }

    public String getFile() {
        return file;
    }

    public String getHost() {
        try {
            return URI.create(url.replaceFirst("^http://pandora\\.nla\\.gov\\.au/pan/\\d+/[0-9-]+/", "http://"))
                    .getHost();
        } catch (Exception e) {
            return null;
        }
    }

    Pattern piMatcher = Pattern.compile("(http://pandora\\.nla\\.gov\\.au/pan/[0-9]+/).*");

    public String getDigestQuery() {
        if (getHost() == null) return null;
        if (getDigest() == null) return null;
        String pandoraBit = "";
        Matcher m = piMatcher.matcher(url);
        if (m.matches()) {
            pandoraBit = m.group(1) + "* ";
        }
        return pandoraBit + getHost() + "/* digest:" + getDigest();
    }
}
