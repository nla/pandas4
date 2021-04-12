package pandas.collection;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static java.time.ZoneOffset.UTC;

public class Capture {
    public static final DateTimeFormatter ARC_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US).withZone(UTC);

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
        this.date = ARC_DATE.parse(fields[1], Instant::from);
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
        return "https://webarchive.nla.gov.au/awa/" + ARC_DATE.format(getDate()) + "/" + getUrl();
    }

    public String getContentType() {
        return contentType;
    }

    public Integer getStatus() {
        return status;
    }

    public String getStatusClass() {
        if (status == null || status < 100) {
            return "status-unknown";
        } else if (status < 300) {
            return "status-2xx";
        } else if (status < 400) {
            return "status-3xx";
        } else if (status < 500) {
            return "status-4xx";
        } else if (status < 600) {
            return "status-5xx";
        } else {
            return "status-unknown";
        }
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
}
