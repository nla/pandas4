package pandas.collection;

import pandas.gather.*;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static pandas.util.Strings.emptyToNull;

public class TitleEditForm {
    private Long id;
    @NotBlank
    private String titleUrl;
    @NotBlank
    private String name;
    @NotNull
    private GatherSchedule gatherSchedule;

    private GatherMethod gatherMethod;

    private List<Instant> oneoffDates = new ArrayList<>();

    @NotNull
    private Format format;
    private String anbdNumber;
    private String localReference;
    private String localDatabaseNo;
    private List<Collection> collections;
    private List<Subject> subjects;
    @Column(name = "IS_CATALOGUING_NOT_REQ")
    private boolean cataloguingNotRequired;
    private String notes;
    private String seedUrls;
    private Status status;
    private boolean legalDeposit;

    private Publisher publisher;
    private String publisherName;
    private PublisherType publisherType;
    private Profile activeProfile;

    public TitleEditForm() {}

    public TitleEditForm(Title title) {
        setAnbdNumber(title.getAnbdNumber());
        setCataloguingNotRequired(title.isCataloguingNotRequired());
        setCollections(title.getCollections());
        setFormat(title.getFormat());
        setId(title.getId());
        setLegalDeposit(title.getLegalDeposit());
        setLocalDatabaseNo(title.getLocalDatabaseNo());
        setLocalReference(title.getLocalReference());
        setName(title.getName());
        setNotes(title.getNotes());
        setPublisher(title.getPublisher());
        setStatus(title.getStatus());
        setSubjects(title.getSubjects());
        setTitleUrl(title.getTitleUrl());
        if (title.getSeedUrl() != null) {
            setSeedUrls(title.getSeedUrl());
        } else {
            setSeedUrls(title.getTitleUrl());
        }

        TitleGather gather = title.getGather();
        if (gather != null) {
            setActiveProfile(gather.getActiveProfile());
            setGatherMethod(gather.getMethod());
            setGatherSchedule(gather.getSchedule());
            setOneoffDates(gather.getOneoffDates().stream().map(GatherDate::getDate).collect(toList()));
            if (gather.getAdditionalUrls() != null && !gather.getAdditionalUrls().isBlank()) {
                setSeedUrls(getSeedUrls() + "\n" + gather.getAdditionalUrls());
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitleUrl() {
        return titleUrl;
    }

    public void setTitleUrl(String titleUrl) {
        this.titleUrl = titleUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GatherSchedule getGatherSchedule() {
        return gatherSchedule;
    }

    public void setGatherSchedule(GatherSchedule gatherSchedule) {
        this.gatherSchedule = gatherSchedule;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public String getAnbdNumber() {
        return anbdNumber;
    }

    public void setAnbdNumber(String anbdNumber) {
        this.anbdNumber = anbdNumber;
    }

    public String getLocalReference() {
        return localReference;
    }

    public void setLocalReference(String localReference) {
        this.localReference = localReference;
    }

    public String getLocalDatabaseNo() {
        return localDatabaseNo;
    }

    public void setLocalDatabaseNo(String localDatabaseNo) {
        this.localDatabaseNo = localDatabaseNo;
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public boolean isCataloguingNotRequired() {
        return cataloguingNotRequired;
    }

    public void setCataloguingNotRequired(boolean cataloguingNotRequired) {
        this.cataloguingNotRequired = cataloguingNotRequired;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public GatherMethod getGatherMethod() {
        return gatherMethod;
    }

    public void setGatherMethod(GatherMethod gatherMethod) {
        this.gatherMethod = gatherMethod;
    }

    public String getSeedUrls() {
        return seedUrls;
    }

    public void setSeedUrls(String seedUrls) {
        this.seedUrls = seedUrls;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Instant> getOneoffDates() {
        return oneoffDates;
    }

    public void setOneoffDates(List<Instant> oneoffDates) {
        this.oneoffDates = oneoffDates;
    }

    public boolean isLegalDeposit() {
        return legalDeposit;
    }

    public void setLegalDeposit(boolean legalDeposit) {
        this.legalDeposit = legalDeposit;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = emptyToNull(publisherName);
    }

    public PublisherType getPublisherType() {
        return publisherType;
    }

    public void setPublisherType(PublisherType publisherType) {
        this.publisherType = publisherType;
    }

    public Profile getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(Profile activeProfile) {
        this.activeProfile = activeProfile;
    }
}
