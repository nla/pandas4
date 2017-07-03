package pandas.admin.marcexport;

import java.util.Date;

public class Title {
    private long id;
    private String name;
    private String titleUrl;
    private Date registeredDate;
    private String publisherName;
    private String publisherUrl;
    private PublisherType publisherType;
    private Format format;
    private Date earliestInstanceDate;

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTitleUrl(String titleUrl) {
        this.titleUrl = titleUrl;
    }

    public void setRegisteredDate(Date registeredDate) {
        this.registeredDate = registeredDate;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public void setPublisherUrl(String publisherUrl) {
        this.publisherUrl = publisherUrl;
    }

    public void setPublisherType(PublisherType publisherType) {
        this.publisherType = publisherType;
    }

    public void setPublisherTypeId(int id) {
        this.publisherType = PublisherType.byId(id);
    }

    public long getId() {

        return id;
    }

    public String getName() {
        return name;
    }

    public String getTitleUrl() {
        return titleUrl;
    }

    public Date getRegisteredDate() {
        return registeredDate;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public String getPublisherUrl() {
        return publisherUrl;
    }


    public PublisherType getPublisherType() {
        return publisherType;
    }

    public void setFormatId(int formatId) {
        this.format = Format.byId(formatId);
    }

    public Format getFormat() {
        return format;
    }

    public Date getEarliestInstanceDate() {
        return earliestInstanceDate;
    }

    public void setEarliestInstanceDate(Date earliestInstanceDate) {
        this.earliestInstanceDate = earliestInstanceDate;
    }
}
