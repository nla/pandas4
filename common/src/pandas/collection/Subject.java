package pandas.collection;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Formula;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@DynamicUpdate
@Indexed
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Subject {
    public static final long CATEGORY_ID_RANGE_START = 15000;
    public static final long CATEGORY_ID_RANGE_END = 15999;

    @Id
    @Column(name = "SUBJECT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SUBJECT_SEQ")
    @SequenceGenerator(name = "SUBJECT_SEQ", sequenceName = "SUBJECT_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    @Column(name = "SUBJECT_NAME")
    @FullTextField(analyzer = "english")
    private String name;

    private String thumbnailUrl;
    private String description;

    @ManyToOne
    @JoinColumn(name = "SUBJECT_PARENT_ID")
    private Subject parent;

    @OneToMany(mappedBy = "parent")
    @OrderBy("name")
    @JsonIgnore
    private List<Subject> children;

    @ManyToMany
    @JoinTable(name = "SUBJECT_TITLES",
            joinColumns = @JoinColumn(name = "SUBJECT_ID"),
            inverseJoinColumns = @JoinColumn(name = "TITLE_ID"),
            indexes = {
                @Index(name = "subject_titles_subject_id_index", columnList = "SUBJECT_ID"),
                @Index(name = "subject_titles_title_id_index", columnList = "TITLE_ID")})
    @OrderBy("name")
    @JsonIgnore
    private List<Title> titles;

    @ManyToMany(mappedBy = "subjects")
    @OrderBy("name")
    List<Collection> collections;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "THUMBNAIL_ID")
    private Thumbnail thumbnail;

    @Formula("(select count(*) from SUBJECT_TITLES st where st.SUBJECT_ID = SUBJECT_ID)")
    private long titleCount;

    @Formula("(select count(*) from COL_SUBS cs where cs.SUBJECT_ID = SUBJECT_ID)")
    private long collectionCount;

    static boolean isInRange(long id) {
        return id >= CATEGORY_ID_RANGE_START && id <= CATEGORY_ID_RANGE_END;
    }

    public static Long toSubjectId(long categoryId) {
        if (!isInRange(categoryId)) throw new IllegalArgumentException("Out of subject range:" + categoryId);
        return categoryId - CATEGORY_ID_RANGE_START;
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

    private transient String fullName;

    @FullTextField(analyzer = "english")
    @IndexingDependency(derivedFrom = {
            @ObjectPath(@PropertyValue(propertyName = "name")),
            @ObjectPath(@PropertyValue(propertyName = "parent"))})
    public String getFullName() {
        if (fullName == null) {
            StringBuilder sb = new StringBuilder();
            for (Subject s : getSubjectBreadcrumbs()) {
                if (sb.length() > 0) {
                    sb.append(" / ");
                }
                sb.append(s.getName());
            }
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
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Subject getParent() {
        return parent;
    }

    public void setParent(Subject parent) {
        this.parent = parent;
    }

    public List<Subject> getChildren() {
        return children;
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public long getTitleCount() {
        return titleCount;
    }

    public long getCollectionCount() {
        return collectionCount;
    }

    @JsonIgnore
    public List<Subject> getSubjectBreadcrumbs() {
        List<Subject> breadcrumbs = new ArrayList<>();
        for (Subject s = this; s != null; s = s.getParent()) {
            breadcrumbs.add(s);
        }
        Collections.reverse(breadcrumbs);
        return breadcrumbs;
    }

    public List<Title> getTitles() {
        return titles;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getDepth() {
        int depth = 0;
        for (Subject s = getParent(); s != null; s = s.getParent()) {
            depth++;
        }
        return depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return Objects.equals(getId(), subject.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
