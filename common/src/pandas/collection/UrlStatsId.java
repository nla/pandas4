package pandas.collection;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public final class UrlStatsId implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private String site;
    private String contentType;
    private int year;

    public UrlStatsId(String site, String contentType, int year) {
        this.site = site;
        this.contentType = contentType;
        this.year = year;
    }

    private UrlStatsId() {
    }

    public String site() {
        return site;
    }

    public String contentType() {
        return contentType;
    }

    public int year() {
        return year;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UrlStatsId) obj;
        return Objects.equals(this.site, that.site) &&
                Objects.equals(this.contentType, that.contentType) &&
                this.year == that.year;
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, contentType, year);
    }

    @Override
    public String toString() {
        return "UrlStatsId[" +
                "site=" + site + ", " +
                "contentType=" + contentType + ", " +
                "year=" + year + ']';
    }

}
