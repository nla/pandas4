package pandas.admin.marcexport;

import java.util.Date;

public class DummyTitle implements Title {
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

    @Override
    public long getId() {

        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitleUrl() {
        return titleUrl;
    }

    @Override
    public Date getRegisteredDate() {
        return registeredDate;
    }

    @Override
    public String getPublisherName() {
        return publisherName;
    }

    @Override
    public String getPublisherUrl() {
        return publisherUrl;
    }

    @Override
    public int getPublisherTypeId() {
        return publisherType.id();
    }


    @Override
    public PublisherType getPublisherType() {
        return publisherType;
    }

    @Override
    public int getFormatId() {
        return format.id();
    }

    public void setFormatId(int formatId) {
        this.format = Format.byId(formatId);
    }

    @Override
    public Format getFormat() {
        return format;
    }

    @Override
    public Date getEarliestInstanceDate() {
        return earliestInstanceDate;
    }

    public void setEarliestInstanceDate(Date earliestInstanceDate) {
        this.earliestInstanceDate = earliestInstanceDate;
    }
}
