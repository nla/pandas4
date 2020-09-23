package pandas.collection;

import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import pandas.agency.Agency;
import pandas.core.Individual;
import pandas.gather.TitleGather;

import javax.persistence.*;
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
public class Title {
    @Id
    @Column(name = "TITLE_ID")
    private Long id;

    @GenericField
    private Long pi;

    @FullTextField(analyzer = "english")
    @KeywordField(name = "name_sort", sortable = Sortable.YES)
    private String name;

    @FullTextField(analyzer = "url")
    private String titleUrl;

    @FullTextField(analyzer = "url")
    private String seedUrl;

    @GenericField(sortable = Sortable.YES)
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
    @IndexedEmbedded(includePaths = {"id", "name"})
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
    private Status status;

    @ManyToMany(mappedBy = "titles")
    @OrderBy("name")
    @IndexedEmbedded(includePaths = {"id", "name"})
    private List<Collection> collections;

    @OneToOne
    @JoinColumn(name = "TITLE_ID")
    @IndexedEmbedded(includePaths = {"schedule.id", "method.id", "notes", "nextGatherDate"})
    // XXX: not sure why it can't find the inverse automatically
    @AssociationInverseSide(
            inversePath = @ObjectPath( @PropertyValue( propertyName = "title" ) )
    )
    private TitleGather gather;

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
        return LocalDate.ofInstant(getRegDate(), ZoneId.systemDefault());
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
}
