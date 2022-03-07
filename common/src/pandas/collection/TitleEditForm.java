package pandas.collection;

import org.springframework.format.annotation.DateTimeFormat;
import pandas.gather.*;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static pandas.util.Strings.emptyToNull;

public class TitleEditForm {
    private Long id;
    @NotBlank
    private String titleUrl;
    @NotBlank
    private String name;
    private GatherSchedule gatherSchedule;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate scheduledDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime scheduledTime;

    private GatherMethod gatherMethod;

    private List<Instant> oneoffDates = new ArrayList<>();

    private Format format;
    private String anbdNumber;
    private String localReference;
    private String localDatabaseNo;
    private List<Collection> collections = new ArrayList<>();
    private List<Subject> subjects = new ArrayList<>();
    @Column(name = "IS_CATALOGUING_NOT_REQ")
    private boolean cataloguingNotRequired = true;
    private String notes;
    private String seedUrls;
    private Status status;
    private Reason reason;
    private boolean legalDeposit = true;

    private Publisher publisher;
    private String publisherName;
    private PublisherType publisherType;
    private Profile activeProfile;
    private Scope scope;

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
            setOneoffDates(gather.getOneoffDates().stream().map(GatherDate::getDate).toList());
            setScope(gather.getScope());
            setScheduledInstant(gather.getScheduledDate());
            if (gather.getAdditionalUrls() != null && !gather.getAdditionalUrls().isBlank()) {
                setSeedUrls(getSeedUrls() + "\n" + gather.getAdditionalUrls());
            }
        }
    }

    public Instant getScheduledInstant() {
        if (scheduledDate == null) return null;
        return LocalDateTime.of(scheduledDate, scheduledTime == null ? LocalTime.MIDNIGHT : scheduledTime)
                .atZone(ZoneId.systemDefault()).toInstant();
    }

    public void setScheduledInstant(Instant instant) {
        if (instant != null) {
            ZonedDateTime scheduledDateZoned = instant.atZone(ZoneId.systemDefault());
            setScheduledDate(scheduledDateZoned.toLocalDate());
            setScheduledTime(scheduledDateZoned.toLocalTime());
        } else {
            setScheduledDate(null);
            setScheduledTime(null);
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
        this.collections.clear();
        this.collections.addAll(collections);
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects.clear();
        this.subjects.addAll(subjects);
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

    public Boolean getLegalDeposit() {
        return legalDeposit;
    }

    public void setLegalDeposit(Boolean legalDeposit) {
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

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
}
