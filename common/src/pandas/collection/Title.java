package pandas.collection;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Formula;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pandas.agency.Agency;
import pandas.core.Individual;
import pandas.gather.Instance;
import pandas.gather.TitleGather;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * An online resource selected for archiving.
 */
@Entity
@Indexed
@NamedEntityGraph(name = "Title.subjects", attributeNodes = @NamedAttributeNode("subjects"))
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(indexes = {
        @Index(name = "title_last_modified_date_title_id_index", columnList = "last_modified_date, title_id"),
        @Index(name = "title_agency_id_current_status_id_index", columnList = "agency_id, current_status_id"),
        @Index(name = "title_current_owner_id_current_status_id_index", columnList = "current_owner_id, current_status_id")})
public class Title {
    @Id
    @Column(name = "TITLE_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "TITLE_SEQ")
    @SequenceGenerator(name = "TITLE_SEQ", sequenceName = "TITLE_SEQ", allocationSize = 1)
// TODO: native generator for mysql support, but this breaks existing h2 schema
//  @GeneratedValue(strategy = GenerationType.AUTO, generator = "TITLE_SEQ")
//  @GenericGenerator(name = "TITLE_SEQ", strategy = "native")
    private Long id;

    /**
     * Persistant Identifier used for referencing archived copies of this online resource.
     * TODO: Unify this with id by changing the id of existing titles to their pi.
     */
    @GenericField
    private Long pi;

    /**
     * The name or heading of this title
     */
    @Column(name = "NAME", nullable = false, length = 256)
    @FullTextField(analyzer = "english")
    @KeywordField(name = "name_sort", sortable = Sortable.YES)
    @NotNull
    private String name;

    /**
     * URL for this resource on the live web.
     */
    @Column(name = "TITLE_URL", length = 1024)
    @FullTextField(analyzer = "url")
    private String titleUrl;

    /**
     * The URL which will be used to gather this title.
     */
    @Column(name = "SEED_URL", length = 1024)
    @FullTextField(analyzer = "url")
    private String seedUrl;

    /**
     * The date this title was created.
     */
    @GenericField(sortable = Sortable.YES)
    @CreatedDate
    @NotNull
    @Column(name = "REG_DATE")
    private Instant regDate;

    /**
     * Format this title is in eg. integrating, serial, monograph.
     */
    @ManyToOne
    @JoinColumn(name = "FORMAT_ID")
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Format format;

    /**
     * The corresponding Title Entry Page used by the display system.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "TEP_ID")
    private Tep tep;

    @ManyToOne
    @JoinColumn(name = "AGENCY_ID", referencedColumnName = "AGENCY_ID")
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Agency agency;

    /**
     * The publishing agency who hold the copyright for this title.
     */
    @ManyToOne
    @JoinColumn(name = "PUBLISHER_ID", referencedColumnName = "PUBLISHER_ID")
    @IndexedEmbedded(includePaths = {"id", "type.id", "organisation.name"})
    private Publisher publisher;

    @ManyToMany
    @JoinTable(name = "SUBJECT_TITLES",
            joinColumns = @JoinColumn(name = "TITLE_ID"),
            inverseJoinColumns = @JoinColumn(name = "SUBJECT_ID"),
            indexes = {@Index(name = "subject_titles_title_id_index", columnList = "title_id"),
                    @Index(name = "subject_titles_collection_id_index", columnList = "subject_id")})
    @OrderBy("name")
    @IndexedEmbedded(includePaths = {"id", "name", "fullName"})
    private List<Subject> subjects;

    @ManyToOne
    @JoinColumn(name = "CURRENT_OWNER_ID")
    @IndexedEmbedded(includePaths = {"id", "nameGiven", "nameFamily", "userid"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Individual owner;

    @OneToMany(mappedBy = "title", orphanRemoval = true, cascade = CascadeType.ALL)
    @OrderBy("date")
    private List<OwnerHistory> ownerHistories = new ArrayList<>();

    @OneToMany(mappedBy = "title", orphanRemoval = true, cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<StatusHistory> statusHistories = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "CURRENT_STATUS_ID")
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    @NotNull
    private Status status;

    @ManyToMany
    @JoinTable(name = "TITLE_COL",
            joinColumns = @JoinColumn(name = "TITLE_ID"),
            inverseJoinColumns = @JoinColumn(name = "COLLECTION_ID"),
            indexes = { @Index(name = "title_col_title_id_index", columnList = "title_id"),
                        @Index(name = "title_col_collection_id_index", columnList = "collection_id") })
    @OrderBy("name")
    @IndexedEmbedded(includePaths = {"id", "name", "fullName"})
    private List<Collection> collections;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "title")
    @IndexedEmbedded(includePaths = {"activeProfile.id", "schedule.id", "method.id", "notes", "nextGatherDate",
            "lastGatherDate", "firstGatherDate"})
    // XXX: not sure why it can't find the inverse automatically
    @AssociationInverseSide(
            inversePath = @ObjectPath( @PropertyValue( propertyName = "title" ) )
    )
    private TitleGather gather;

    @Formula("(select MIN(i.INSTANCE_DATE) from INSTANCE i where i.TITLE_ID = TITLE_ID)")
    private Instant firstInstanceDate;

    /**
     * Internal notes about this title.
     */
    @Column(name = "NOTES", length = 4000)
    private String notes;

    @GenericField(sortable = Sortable.YES)
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    private Instant lastModifiedDate;

    /**
     * Notes about any offensive content within this title
     */
    @Column(name = "CONTENT_WARNING", length = 256)
    private String contentWarning;

    /**
     * Australian National Bibliographic Database catalogue record identifier for this archived resource
     */
    @Column(name = "ANBD_NUMBER", length = 22)
    private String anbdNumber;

    /**
     * An agency specific database number for this title. At the NLA, this is a Voyager database number.
     */
    @Column(name = "LOCAL_DATABASE_NO", length = 25)
    private String localDatabaseNo;

    /**
     * An agency specific reference number for this title. At the NLA, this is a TRIM number.
     */
    @Column(name = "LOCAL_REFERENCE", length = 25)
    private String localReference;

    /**
     * Flags whether cataloguing is required for this title or not.
     */
    @Column(name = "IS_CATALOGUING_NOT_REQ")
    @NotNull
    private boolean cataloguingNotRequired;

    /**
     * Indicates whether this title must be subscribed to before it can be accessesd.
     */
    @Column(name = "IS_SUBSCRIPTION")
    @NotNull
    private boolean subscription;

    @NotNull
    @Column(name = "LEGAL_DEPOSIT")
    private boolean legalDeposit;

    @NotNull
    @Column(name = "UNABLE_TO_ARCHIVE")
    private boolean unableToArchive;

    @NotNull
    @Column(name = "DISAPPEARED")
    private boolean disappeared;

    /**
     * Flags whether this title is waiting to be acknowledged after a transfer of ownership.
     */
    @NotNull
    @Column(name = "AWAITING_CONFIRMATION")
    private boolean awaitingConfirmation;

    /**
     * PURLs which were stored for titles in a previous version of the system, no longer added or edited but need to
     * be stored to maintain their persistence.
     */
    @Column(name = "LEGACY_PURL", length = 1024)
    private String legacyPurl;

    /**
     * Shortened name used for display in worktrays.
     */
    @Column(name = "SHORT_DISPLAY_NAME", length = 256)
    private String shortDisplayName;

    /**
     * Unused. TODO: remove this column.
     */
    @Deprecated
    @Column(name = "TITLE_RESOURCE_ID")
    private Long titleResourceId;

    @OneToMany(mappedBy = "title")
    @OrderBy("date")
    private List<Instance> instances;

    /**
     * Foreign key to the old Pv2 standing for this title
     */
    @Deprecated
    @Column(name="STANDING_ID")
    private Long standingId;

    /**
     * Foreign key to the old Pv2 status of this title
     */
    @Deprecated
    @Column(name = "STATUS_ID")
    private Long statusId;

    /**
     * Foreign key to the indexing agency which asked for this title to be nominated.
     */
    @Column(name = "INDEXER_ID")
    private Long indexerId;

    /**
     * The active permission for this title. The active permission may be a title level permission or a publisher
     * blanket permission
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERMISSION_ID")
    private Permission permission;

    /**
     * The title level permission for this title. (May or may not be the active permission for this title.)
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "DEFAULT_PERMISSION_ID")
    private Permission defaultPermission;

    @OneToMany(mappedBy = "title", orphanRemoval = true, cascade = CascadeType.ALL)
    @OrderBy("date")
    private List<Contact> contactEvents = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "TITLE_INDIVIDUAL",
            joinColumns = @JoinColumn(name = "TITLE_ID"),
            inverseJoinColumns = @JoinColumn(name = "INDIVIDUAL_ID"))
    private List<Individual> contactPeople = new ArrayList<>();

    @OneToMany(mappedBy = "title", orphanRemoval = true, cascade = CascadeType.ALL)
    @OrderBy("date desc")
    private List<TitlePreviousName> previousNames = new ArrayList<>();

    @OneToMany(mappedBy = "ceased")
    @OrderBy("date")
    private List<TitleHistory> continuedBy;

    @OneToMany(mappedBy = "continues")
    @OrderBy("date")
    private List<TitleHistory> continues;

    public List<String> getAllSeeds() {
        List<String> seeds = new ArrayList<>();
        if (getGather() != null && getGather().getGatherUrl() != null) {
            seeds.add(getGather().getGatherUrl());
        } else if (getSeedUrl() != null) {
            seeds.add(getSeedUrl());
        } else if (getTitleUrl() != null) {
            seeds.add(getTitleUrl());
        }
        TitleGather gather = getGather();
        if (gather != null && gather.getAdditionalUrls() != null) {
            for (String url: gather.getAdditionalUrls().split("\\s+")) {
                url = url.trim();
                if (!url.isBlank()) {
                    seeds.add(url);
                }
            }
        }
        return seeds;
    }

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPi() {
        return pi;
    }

    public void setPi(Long pi) {
        this.pi = pi;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public Instant getRegDate() {
        return regDate;
    }

    public void setRegDate(Instant regDate) {
        this.regDate = regDate;
    }

    public LocalDate getRegDateLocal() {
        return getRegDate() == null ? null : LocalDate.ofInstant(getRegDate(), ZoneId.systemDefault());
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public Tep getTep() {
        return tep;
    }

    public String getTitleUrl() {
        return titleUrl;
    }

    public void setTitleUrl(String titleUrl) {
        this.titleUrl = titleUrl;
    }

    public boolean isVisible() {
        return getTep() != null && getTep().getDoCollection() != null && getTep().getDoCollection();
    }

    public Individual getOwner() {
        return owner;
    }

    public void setOwner(Individual owner) {
        this.owner = owner;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getThumbnailUrl() {
        return "https://pandas.nla.gov.au/api/image?url=" +
                URLEncoder.encode("https://web.archive.org.au/awa-nobanner/29990730022559/" + getTitleUrl(), UTF_8) +
                "&clip=240,50,800,500,0.4";
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
    }

    @OneToMany(mappedBy = "title")
    private java.util.Collection<Thumbnail> thumbnails;

    public java.util.Collection<Thumbnail> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(java.util.Collection<Thumbnail> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public String getSeedUrl() {
        return seedUrl;
    }

    public void setSeedUrl(String seedUrl) {
        this.seedUrl = seedUrl;
    }

    public TitleGather getGather() {
        return gather;
    }

    public void setGather(TitleGather gather) {
        this.gather = gather;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public Instant getFirstInstanceDate() {
        return firstInstanceDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getAnbdNumber() {
        return anbdNumber;
    }

    public void setAnbdNumber(String anbdNumber) {
        this.anbdNumber = anbdNumber;
    }

    public String getLocalDatabaseNo() {
        return localDatabaseNo;
    }

    public void setLocalDatabaseNo(String localDatabaseNo) {
        this.localDatabaseNo = localDatabaseNo;
    }

    public String getLocalReference() {
        return localReference;
    }

    public void setLocalReference(String localReference) {
        this.localReference = localReference;
    }

    public boolean isCataloguingNotRequired() {
        return cataloguingNotRequired;
    }

    public void setCataloguingNotRequired(boolean cataloguingNotRequired) {
        this.cataloguingNotRequired = cataloguingNotRequired;
    }

    public boolean isSubscription() {
        return subscription;
    }

    public void setSubscription(boolean subscription) {
        this.subscription = subscription;
    }

    public boolean getLegalDeposit() {
        return legalDeposit;
    }

    public void setLegalDeposit(boolean legalDeposit) {
        this.legalDeposit = legalDeposit;
    }

    public boolean isUnableToArchive() {
        return unableToArchive;
    }

    public void setUnableToArchive(boolean unableToArchive) {
        this.unableToArchive = unableToArchive;
    }

    public String getContentWarning() {
        return contentWarning;
    }

    public void setContentWarning(String contentWarning) {
        this.contentWarning = contentWarning;
    }

    public boolean isDisappeared() {
        return disappeared;
    }

    public void setDisappeared(boolean disappeared) {
        this.disappeared = disappeared;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public boolean isAwaitingConfirmation() {
        return awaitingConfirmation;
    }

    public void setAwaitingConfirmation(boolean awaitingConfirmation) {
        this.awaitingConfirmation = awaitingConfirmation;
    }

    public String getLegacyPurl() {
        return legacyPurl;
    }

    public void setLegacyPurl(String legacyPurl) {
        this.legacyPurl = legacyPurl;
    }

    public String getDisplayName() {
        if (getTep() != null && getTep().getDisplayTitle() != null) {
            return getTep().getDisplayTitle();
        }
        return getName();
    }

    public String getShortDisplayName() {
        return shortDisplayName;
    }

    public void setShortDisplayName(String shortDisplayName) {
        this.shortDisplayName = shortDisplayName;
    }

    @Deprecated
    public Long getTitleResourceId() {
        return titleResourceId;
    }

    @Deprecated
    public void setTitleResourceId(Long titleResourceId) {
        this.titleResourceId = titleResourceId;
    }

    @Deprecated
    public Long getStandingId() {
        return standingId;
    }

    @Deprecated
    public void setStandingId(Long standingId) {
        this.standingId = standingId;
    }

    @Deprecated
    public Long getStatusId() {
        return statusId;
    }

    @Deprecated
    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public Long getIndexerId() {
        return indexerId;
    }

    public void setIndexerId(Long indexerId) {
        this.indexerId = indexerId;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public Permission getDefaultPermission() {
        return defaultPermission;
    }

    public void setDefaultPermission(Permission defaultPermission) {
        this.defaultPermission = defaultPermission;
    }

    public String getYearRange() {
        int start = getRegDateLocal().getYear();
        int end = start;
        if (!getInstances().isEmpty()) {
            end = Collections.max(getInstances(), Comparator.comparing(Instance::getDate)).getDate().atZone(ZoneId.systemDefault()).getYear();
        }
        if (start == end) {
            return Integer.toString(start);
        } else {
            return start + "-" + end;
        }
    }

    public List<OwnerHistory> getOwnerHistories() {
        return ownerHistories;
    }

    public Individual getNominator() {
        if (getOwnerHistories().isEmpty()) {
            return null;
        }
        return getOwnerHistories().get(0).getIndividual();
    }

    public List<Contact> getContactEvents() {
        return contactEvents;
    }

    public void setContactEvents(List<Contact> contactEvents) {
        this.contactEvents = contactEvents;
    }

    public List<Individual> getContactPeople() {
        return contactPeople;
    }

    public Instant getLastContactDate() {
        List<Contact> events = getContactEvents();
        if (events.isEmpty()) return null;
        return events.get(events.size() - 1).getDate();
    }

    /**
     * Returns true if the given user is the owner of this title.
     */
    public boolean isOwnedBy(Individual user) {
        if (getOwner() == null || user == null) return false;
        if (user == getOwner()) return true;
        return user.getId() != null && user.getId().equals(getOwner().getId());
    }

    /**
     * Returns true if the given agency is the assigned agency of this title or if the owner of this title is a member
     * of the given agency.
     */
    public boolean isOwnedBy(Agency agency) {
        if (getAgency() == null || agency == null) return false;
        if (getAgency() == agency) return true;
        if (agency.getId() != null && agency.getId().equals(getAgency().getId())) return true;
        return getOwner() != null && getOwner().getAgency() != agency && isOwnedBy(getOwner().getAgency());
    }

    public List<TitlePreviousName> getPreviousNames() {
        return previousNames;
    }

    public List<TitleHistory> getContinuedBy() {
        return continuedBy;
    }

    public List<TitleHistory> getContinues() {
        return continues;
    }

    public boolean isScheduled() {
        if (getGather() == null) return false;
        if (getGather().getSchedule() == null) return false;
        return !"None".equals(getGather().getSchedule().getName());
    }

    public void setTep(Tep tep) {
        this.tep = tep;
    }

    public List<StatusHistory> getStatusHistories() {
        return statusHistories;
    }
}
