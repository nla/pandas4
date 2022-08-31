package pandas.collection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(UrlStatsId.class)
public class UrlStats {
    @Id
    private String site;
    @Id
    private String contentType;
    @Id
    private int year;
    private long snapshots;
    private long sumOfContentLengths;

    public UrlStatsId id() {
        return new UrlStatsId(site, contentType, year);
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(long snapshots) {
        this.snapshots = snapshots;
    }

    public long getSumOfContentLengths() {
        return sumOfContentLengths;
    }

    public void setSumOfContentLengths(long sumOfContentLengths) {
        this.sumOfContentLengths = sumOfContentLengths;
    }

    public String toString() {
        return site + " " + contentType + " " + year + " " + snapshots + " " + sumOfContentLengths;
    }
}
