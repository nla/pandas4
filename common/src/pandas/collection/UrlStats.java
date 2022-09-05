package pandas.collection;

import org.springframework.data.domain.Persistable;

import javax.persistence.*;

@Entity
@IdClass(UrlStatsId.class)
public class UrlStats implements Persistable<UrlStatsId> {
    @Id
    private String site;
    @Id
    private String contentType;
    @Id
    private int year;
    private long snapshots;
    private long totalContentLength;

    @Transient
    private boolean isNew = true;

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

    public long getTotalContentLength() {
        return totalContentLength;
    }

    public void setTotalContentLength(long sumOfContentLengths) {
        this.totalContentLength = sumOfContentLengths;
    }

    public String toString() {
        return site + " " + contentType + " " + year + " " + snapshots + " " + totalContentLength;
    }

    @Override
    public UrlStatsId getId() {
        return id();
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
