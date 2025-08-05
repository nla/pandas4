package pandas.collection;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pandas.agency.User;
import pandas.core.UseIdentityGeneratorIfMySQL;
import pandas.core.View;
import pandas.gather.GatherSchedule;
import pandas.util.TimeFrame;

import java.sql.Types;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "COL")
@DynamicUpdate
@Indexed
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@EntityListeners(AuditingEntityListener.class)
public class Collection {
    private static final Logger log = LoggerFactory.getLogger(Collection.class);
    public static final Comparator<Collection> COMPARE_BY_FULL_NAME = Comparator.comparing(Collection::getFullName)
            .thenComparing(Collection::getId);

    @Id
    @Column(name = "COL_ID")
    @UseIdentityGeneratorIfMySQL
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COL_SEQ")
    @SequenceGenerator(name = "COL_SEQ", sequenceName = "COL_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES, sortable = Sortable.YES)
    @JsonView({View.Summary.class, View.CollectionSearchResults.class})
    private Long id;

    @Lob
    @JdbcTypeCode(Types.LONGVARCHAR)
    private String displayComment;
    private Integer displayOrder;

    @GenericField(aggregable = Aggregable.YES)
    private boolean isDisplayed = true;

    private String thumbnailUrl;

    @FullTextField(analyzer = "english")
    @JsonView({View.Summary.class, View.CollectionSearchResults.class})
    private String name;

    @ManyToOne
    @JoinColumn(name = "COL_PARENT_ID")
    private Collection parent;

    @OneToMany(mappedBy = "parent")
    @OrderBy("name")
    private List<Collection> children = new ArrayList<>();

    @ManyToMany(mappedBy = "collections")
    @OrderBy("name")
    private List<Title> titles = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "COL_SUBS",
            joinColumns = @JoinColumn(name = "COL_ID"),
            inverseJoinColumns = @JoinColumn(name = "SUBJECT_ID"),
            indexes = { @Index(name = "col_subs_col_id_index", columnList = "col_id"),
                        @Index(name = "col_subs_subject_id_index", columnList = "subject_id") })
    @OrderBy("name")
    @IndexedEmbedded(includePaths = {"id", "name"})
    private List<Subject> subjects = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "THUMBNAIL_ID")
    private Thumbnail thumbnail;

    private Instant startDate;

    private Instant endDate;

    @ManyToOne
    @JoinColumn(name = "GATHER_SCHEDULE_ID")
    private GatherSchedule gatherSchedule;

    @CreatedDate
    @GenericField(sortable = Sortable.YES)
    private Instant createdDate;

    @LastModifiedDate
    private Instant lastModifiedDate;

    @CreatedBy
    @ManyToOne
    @JoinColumn(name = "CREATED_BY")
    private User createdBy;

    @LastModifiedBy
    @ManyToOne
    @JoinColumn(name = "LAST_MODIFIED_BY")
    private User lastModifiedBy;

    @GenericField
    private boolean closed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayComment() {
        return displayComment;
    }

    public void setDisplayComment(String displayComment) {
        this.displayComment = displayComment;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isDisplayed() {
        return isDisplayed;
    }

    public void setDisplayed(boolean displayed) {
        isDisplayed = displayed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private transient String fullName;

    @FullTextField(analyzer = "english")
    @KeywordField(name="name_sort", sortable = Sortable.YES)
    @IndexingDependency(derivedFrom = {
            @ObjectPath(@PropertyValue(propertyName = "name")),
            @ObjectPath({@PropertyValue(propertyName = "parent")}),
            @ObjectPath({@PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "parent")}),
            @ObjectPath({@PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "parent")}),
            @ObjectPath({@PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "name")}),
            @ObjectPath({@PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "name")}),
            @ObjectPath({@PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "name")})})
    @JsonView({View.Summary.class, View.CollectionSearchResults.class})
    public String getFullName() {
        if (fullName == null) {
            StringBuilder sb = new StringBuilder();
            for (Collection c : getCollectionBreadcrumbs()) {
                sb.append(c.getName());
                sb.append("—");
            }
            sb.append(getName());
            fullName = sb.toString();
        }
        return fullName;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getDescription() {
        return getDisplayComment();
    }

    public void setDescription(String description) {
        setDisplayComment(description);
    }

    public List<Collection> getCollectionBreadcrumbs() {
        List<Collection> breadcrumbs = new ArrayList<>();
        for (Collection c = this.getParent(); c != null; c = c.getParent()) {
            breadcrumbs.add(c);
        }
        Collections.reverse(breadcrumbs);
        return breadcrumbs;
    }

    public Collection getTopLevel() {
        Collection c = this;
        while (c.getParent() != null) {
             c = c.getParent();
        }
        return c;
    }

    public Collection getParent() {
        return parent;
    }

    public void setParent(Collection parent) {
        this.parent = parent;
    }

    public List<Collection> getChildren() {
        return children;
    }

    public void setChildren(List<Collection> children) {
        this.children = children;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    /**
     * Returns the subjects for this collection or the subjects inherited from its parent if there are none.
     */
    @JsonView(View.CollectionSearchResults.class)
    public List<Subject> getInheritedSubjects() {
        Collection collection = this;
        // if the collection has no subjects, inherit them from its parent
        while (collection.getSubjects().isEmpty() && collection.getParent() != null) {
            collection = collection.getParent();
        }
        return collection.getSubjects();
    }

    public String getInheritedSubjectIdString() {
        return getInheritedSubjects().stream().map(Subject::getId)
                .map(String::valueOf).collect(Collectors.joining(","));
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public List<Title> getTitles() {
        return titles;
    }

    @PreRemove
    public void removeFromAllTitles() {
        for (var title : getTitles()) {
            title.removeCollection(this);
        }
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public TimeFrame getTimeFrame() {
        if (startDate == null && endDate == null) return null;
        return new TimeFrame(startDate, endDate);
    }

    public TimeFrame getInheritedTimeFrame() {
        for (var collection = this; collection != null; collection = collection.getParent()) {
            TimeFrame timeFrame = collection.getTimeFrame();
            if (timeFrame != null) return timeFrame;
        }
        return null;
    }

    public boolean coversOneMonth() {
        if (startDate == null) return false;
        if (endDate == null) return false;
        return startDate.atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)
                .equals(endDate.atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1));
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public GatherSchedule getGatherSchedule() {
        return gatherSchedule;
    }

    public void setGatherSchedule(GatherSchedule gatherSchedule) {
        if (gatherSchedule == this.gatherSchedule) return;
        this.gatherSchedule = gatherSchedule;
        for (Title title: getTitles()) {
            title.getGather().calculateNextGatherDate();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass() || getId() == null) return false;
        Collection that = (Collection) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /**
     * Returns "" for top-level collections, "sub" for subcollections, "subsub" for subsubcollections.
     */
    public String getDepthPrefix() {
        StringBuilder builder = new StringBuilder();
        for (Collection ancester = getParent(); ancester != null; ancester = ancester.getParent()) {
            builder.append("sub");
        }
        return builder.toString();
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    @GenericField
    @IndexingDependency(derivedFrom = {
            @ObjectPath(@PropertyValue(propertyName = "closed")),
            @ObjectPath({@PropertyValue(propertyName = "parent")}),
            @ObjectPath({@PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "parent")}),
            @ObjectPath({@PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "parent")}),
            @ObjectPath({@PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "closed")}),
            @ObjectPath({@PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "closed")}),
            @ObjectPath({@PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "parent"), @PropertyValue(propertyName = "closed")})})
    public boolean isAncestorClosed() {
        for (var col = this; col != null; col = col.getParent()) {
            if (col.isClosed()) return true;
        }
        return false;
    }
}