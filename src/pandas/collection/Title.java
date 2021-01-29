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
import pandas.gather.TitleGather;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Entity
@Indexed
@NamedEntityGraph(name = "Title.subjects",
        attributeNodes = @NamedAttributeNode("subjects"))
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Title {
    @Id
    @Column(name = "TITLE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TITLE_SEQ")
    @SequenceGenerator(name = "TITLE_SEQ", sequenceName = "TITLE_SEQ", allocationSize = 1)
    private Long id;

    @GenericField
    private Long pi;

    @FullTextField(analyzer = "english")
    @KeywordField(name = "name_sort", sortable = Sortable.YES)
    @NotNull
    private String name;

    @FullTextField(analyzer = "url")
    private String titleUrl;

    @FullTextField(analyzer = "url")
    private String seedUrl;

    @GenericField(sortable = Sortable.YES)
    @CreatedDate
    @NotNull
    private Instant regDate;

    @ManyToOne
    @JoinColumn(name = "FORMAT_ID")
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Format format;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEP_ID")
    private Tep tep;

    @ManyToOne
    @JoinColumn(name = "AGENCY_ID", referencedColumnName = "AGENCY_ID")
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Agency agency;

    @ManyToOne
    @JoinColumn(name = "PUBLISHER_ID", referencedColumnName = "PUBLISHER_ID")
    @IndexedEmbedded(includePaths = {"id", "type.id", "organisation.name"})
    private Publisher publisher;

    @ManyToMany(mappedBy = "titles")
    @OrderBy("name")
    @IndexedEmbedded(includePaths = {"id", "name", "fullName"})
    private List<Subject> subjects;

    @ManyToOne
    @JoinColumn(name = "CURRENT_OWNER_ID")
    @IndexedEmbedded(includePaths = {"id", "nameGiven", "nameFamily", "userid"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Individual owner;

    @ManyToOne
    @JoinColumn(name = "CURRENT_STATUS_ID")
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    @NotNull
    private Status status;

    @ManyToMany
    @JoinTable(name = "TITLE_COL",
            joinColumns = @JoinColumn(name = "TITLE_ID"),
            inverseJoinColumns = @JoinColumn(name = "COLLECTION_ID"))
    @OrderBy("name")
    @IndexedEmbedded(includePaths = {"id", "name", "fullName"})
    private List<Collection> collections;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "title")
    @IndexedEmbedded(includePaths = {"schedule.id", "method.id", "notes", "nextGatherDate", "lastGatherDate", "firstGatherDate"})
    // XXX: not sure why it can't find the inverse automatically
    @AssociationInverseSide(
            inversePath = @ObjectPath( @PropertyValue( propertyName = "title" ) )
    )
    private TitleGather gather;

    @Formula("(select MIN(i.INSTANCE_DATE) from INSTANCE i where i.TITLE_ID = TITLE_ID)")
    private Instant firstInstanceDate;

    private String notes;

    @GenericField(sortable = Sortable.YES)
    @LastModifiedDate
    private Instant lastModifiedDate;

    private String contentWarning;
    private String anbdNumber;
    private String localDatabaseNo;
    private String localReference;
    @Column(name = "IS_CATALOGUING_NOT_REQ")
    @NotNull
    private boolean cataloguingNotRequired;
    @Column(name = "IS_SUBSCRIPTION")
    @NotNull
    private boolean subscription;
    @NotNull
    private boolean legalDeposit;
    @NotNull
    private boolean unableToArchive;
    @NotNull
    private boolean disappeared;

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
        return getTep() != null && getTep().isDoCollection();
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
}
