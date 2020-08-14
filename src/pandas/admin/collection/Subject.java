package pandas.admin.collection;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Formula;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.springframework.web.bind.annotation.PathVariable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Entity
@DynamicUpdate
@Indexed
public class Subject extends AbstractCategory {
    public static final long CATEGORY_ID_RANGE_START = 15000;
    public static final long CATEGORY_ID_RANGE_END = 15999;

    @Id
    @Column(name = "SUBJECT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SUBJECT_SEQ")
    @SequenceGenerator(name = "SUBJECT_SEQ", sequenceName = "SUBJECT_SEQ", allocationSize = 1)
    @GenericField
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
    private List<Subject> children;

    @ManyToMany
    @JoinTable(name = "SUBJECT_TITLES",
            joinColumns = @JoinColumn(name = "SUBJECT_ID"),
            inverseJoinColumns = @JoinColumn(name = "TITLE_ID"))
    @OrderBy("name")
    private List<Title> titles;

    @ManyToMany
    @JoinTable(name = "COL_SUBS",
            joinColumns = @JoinColumn(name = "SUBJECT_ID"),
            inverseJoinColumns = @JoinColumn(name = "COL_ID"))
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

    @Override
    public Long getCategoryId() {
        return getId() == null ? null : CATEGORY_ID_RANGE_START + getId();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public Subject getParent() {
        return parent;
    }

    public void setParent(Subject parent) {
        this.parent = parent;
    }

    public List<Category> getSubcategories() {
        ArrayList<Category> subcategories = new ArrayList<>();
        subcategories.addAll(getChildren());
        subcategories.addAll(getCollections());
        subcategories.sort(Comparator.comparing(Category::getName));
        return subcategories;
    }

    @Override
    public List<Category> getParents() {
        Subject parent = getParent();
        return parent == null ? List.of() : List.of(parent);
    }

    @Override
    public Category getParentCategory() {
        return getParent();
    }

    @Override
    public void setParentCategory(Category parent) {
        if (parent instanceof Subject) {
            setParent((Subject) parent);
        } else if (parent.getCategoryId() == 0) {
            setParent(null);
        } else {
            throw new IllegalArgumentException(parent.getClass().getName());
        }
    }

    @Override
    public String getType() {
        return getClass().getSimpleName();
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

    public List<Subject> getSubjectBreadcrumbs() {
        List<Subject> breadcrumbs = new ArrayList<>();
        for (Subject s = this; s != null; s = s.getParent()) {
            breadcrumbs.add(s);
        }
        Collections.reverse(breadcrumbs);
        return breadcrumbs;
    }

    @Override
    public List<Title> getTitles() {
        return titles;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }
}
