package pandas.collection;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pandas.core.Individual;

import javax.persistence.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "COL")
@DynamicUpdate
@Indexed
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@EntityListeners(AuditingEntityListener.class)
public class Collection {
    @Id
    @Column(name = "COL_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COL_SEQ")
    @SequenceGenerator(name = "COL_SEQ", sequenceName = "COL_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String displayComment;
    private Integer displayOrder;
    private boolean isDisplayed;
    private String thumbnailUrl;

    @FullTextField(analyzer = "english")
    private String name;

    @ManyToOne
    @JoinColumn(name = "COL_PARENT_ID")
    private Collection parent;

    @OneToMany(mappedBy = "parent")
    @OrderBy("name")
    private List<Collection> children = new ArrayList<>();

    @ManyToMany(mappedBy = "collections")
    @OrderBy("name")
    private List<Title> titles;

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

    @Formula("((select count(*) from TITLE_COL tc where tc.COLLECTION_ID = COL_ID) +" +
            "  (select count(*) from TITLE_COL tc left join COL c on c.COL_ID = tc.COLLECTION_ID where c.COL_PARENT_ID = COL_ID))")
    private long titleCount;

    private Instant startDate;

    private Instant endDate;

    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant lastModifiedDate;

    @CreatedBy
    @ManyToOne
    @JoinColumn(name = "CREATED_BY")
    private Individual createdBy;

    @LastModifiedBy
    @ManyToOne
    @JoinColumn(name = "LAST_MODIFIED_BY")
    private Individual lastModifiedBy;

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
            @ObjectPath(@PropertyValue(propertyName = "parent"))})
    public String getFullName() {
        if (fullName == null) {
            StringBuilder sb = new StringBuilder();
            for (Collection c : getCollectionBreadcrumbs()) {
                sb.append(c.getName());
                sb.append(" / ");
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

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public List<Title> getTitles() {
        return titles;
    }

    @PreRemove
    public void removeFromAllTitles() {
        for (var title : getTitles()) {
            title.getCollections().remove(this);
        }
    }

    public long getTitleCount() {
        return titleCount;
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

    public boolean hasTimePeriod() {
        return startDate != null || endDate != null;
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

    public Individual getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Individual createdBy) {
        this.createdBy = createdBy;
    }

    public Individual getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(Individual lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}
