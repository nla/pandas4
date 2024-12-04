package pandas.collection;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import pandas.gather.*;

import java.time.*;
import java.util.*;

import static pandas.util.Strings.emptyToNull;

public class TitleEditForm {
    private Long id;
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
    private final Set<Collection> collections = new TreeSet<>(Collection.COMPARE_BY_FULL_NAME);
    private final Set<Subject> subjects = new TreeSet<>(Subject.COMPARE_BY_NAME);
    @Column(name = "IS_CATALOGUING_NOT_REQ")
    private boolean cataloguingNotRequired = true;
    private String notes;
    private String seedUrls;
    private String filters;
    private boolean ignoreRobotsTxt;
    private Status status;
    private Reason reason;
    private boolean unableToArchive;
    private boolean disappeared;

    private Publisher publisher;
    private String publisherName;
    private PublisherType publisherType;
    private String publisherAbn;
    private Profile activeProfile;
    private Scope scope;
    private Title continues;
    private Set<Title> continuesTitles = new TreeSet<>(Title.COMPARE_BY_NAME);
    private Set<Title> continuedByTitles = new TreeSet<>(Title.COMPARE_BY_NAME);
    private PermissionTypeRadio permissionType;
    private String permissionState;
    private PermissionEditForm titlePermission;
    private Permission publisherPermission;

    public PermissionEditForm getTitlePermission() {
        return titlePermission;
    }

    public TitleEditForm setTitlePermission(PermissionEditForm titlePermission) {
        this.titlePermission = titlePermission;
        return this;
    }

    public Permission getPublisherPermission() {
        return publisherPermission;
    }

    public TitleEditForm setPublisherPermission(Permission publisherPermission) {
        this.publisherPermission = publisherPermission;
        return this;
    }

    public boolean getIgnoreRobotsTxt() {
        return ignoreRobotsTxt;
    }

    public void setIgnoreRobotsTxt(boolean ignoreRobotsTxt) {
        this.ignoreRobotsTxt = ignoreRobotsTxt;
    }

    enum PermissionTypeRadio {
        LEGAL_DEPOSIT, TITLE, PUBLISHER
    }

    public TitleEditForm() {}

    public TitleEditForm(Title title) {
        setAnbdNumber(title.getAnbdNumber());
        setCataloguingNotRequired(title.isCataloguingNotRequired());
        setCollections(title.getCollections());
        title.getContinuedBy().forEach(titleHistory -> continuedByTitles.add(titleHistory.getContinues()));
        title.getContinues().forEach(titleHistory -> continuesTitles.add(titleHistory.getCeased()));
        setDisappeared(title.isDisappeared());
        setFormat(title.getFormat());
        setId(title.getId());
        setLocalDatabaseNo(title.getLocalDatabaseNo());
        setLocalReference(title.getLocalReference());
        setName(title.getName());
        setNotes(title.getNotes());
        setPublisher(title.getPublisher());
        setStatus(title.getStatus());
        setSubjects(title.getSubjects());
        if (title.getSeedUrl() != null) {
            setSeedUrls(title.getSeedUrl());
        } else {
            setSeedUrls(title.getTitleUrl());
        }

        // if the title url is the same as the first seed url, leave it blank on the form
        if (title.getSeedUrl() != null &&
            (title.getSeedUrl().equals(title.getTitleUrl()) ||
                title.getSeedUrl().startsWith(title.getTitleUrl() + "\n"))) {
            setTitleUrl(null);
        } else {
            setTitleUrl(title.getTitleUrl());
        }

        setUnableToArchive(title.isUnableToArchive());

        TitleGather gather = title.getGather();
        if (gather != null) {
            setActiveProfile(gather.getActiveProfile());
            setFilters(gather.getFilters() == null ? null : gather.getFilters().replace(' ', '\n'));
            setGatherMethod(gather.getMethod());
            setGatherSchedule(gather.getSchedule());
            setOneoffDates(gather.getOneoffDates().stream().map(GatherDate::getDate).toList());
            setIgnoreRobotsTxt(gather.getIgnoreRobotsTxt());
            setScope(gather.getScope());
            setScheduledInstant(gather.getScheduledDate());
            if (gather.getAdditionalUrls() != null && !gather.getAdditionalUrls().isBlank()) {
                setSeedUrls(getSeedUrls() + "\n" + gather.getAdditionalUrls());
            }
        }

        if (title.getLegalDeposit()) {
            permissionType = PermissionTypeRadio.LEGAL_DEPOSIT;
        } else if (Objects.equals(title.getPermission(), title.getDefaultPermission())) {
            permissionType = PermissionTypeRadio.TITLE;
        } else {
            permissionType = PermissionTypeRadio.PUBLISHER;
        }

        Permission titlePermission = title.getDefaultPermission();
        if (titlePermission != null) {
            this.titlePermission = PermissionEditForm.from(titlePermission);
        } else {
            this.titlePermission = new PermissionEditForm();
        }

        Permission permission = title.getPermission();
        if (permission != null && permission.isBlanket()) {
            publisherPermission = permission;
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

    public Set<Collection> getCollections() {
        return collections;
    }

    public void setCollections(Set<Collection> collections) {
        this.collections.clear();
        this.collections.addAll(collections);
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(java.util.Collection<Subject> subjects) {
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

    public boolean isUnableToArchive() {
        return unableToArchive;
    }

    public void setUnableToArchive(boolean unableToArchive) {
        this.unableToArchive = unableToArchive;
    }

    public Title getContinues() {
        return continues;
    }

    public void setContinues(Title continues) {
        this.continues = continues;
    }

    public void setDisappeared(boolean disappeared) {
        this.disappeared = disappeared;
    }

    public boolean isDisappeared() {
        return disappeared;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public String getPublisherAbn() {
        return publisherAbn;
    }

    public void setPublisherAbn(String publisherAbn) {
        this.publisherAbn = publisherAbn;
    }

    public Set<Title> getContinuesTitles() {
        return continuesTitles;
    }

    public void setContinuesTitles(Set<Title> continuesTitles) {
        this.continuesTitles.clear();
        this.continuesTitles.addAll(continuesTitles);
    }

    public Set<Title> getContinuedByTitles() {
        return continuedByTitles;
    }

    public void setContinuedByTitles(Set<Title> continuedByTitles) {
        this.continuedByTitles.clear();
        this.continuedByTitles.addAll(continuedByTitles);
    }

    public PermissionTypeRadio getPermissionType() {
        return permissionType;
    }

    public TitleEditForm setPermissionType(PermissionTypeRadio permissionType) {
        this.permissionType = permissionType;
        return this;
    }
}
