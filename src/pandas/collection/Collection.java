package pandas.collection;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Formula;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "COL")
@DynamicUpdate
@Indexed
public class Collection extends AbstractCategory {
    @Id
    @Column(name = "COL_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COL_SEQ")
    @SequenceGenerator(name = "COL_SEQ", sequenceName = "COL_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

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

    @ManyToMany
    @JoinTable(name = "TITLE_COL",
            joinColumns = @JoinColumn(name = "COLLECTION_ID"),
            inverseJoinColumns = @JoinColumn(name = "TITLE_ID"))
    @OrderBy("name")
    private List<Title> titles;

    @ManyToMany(mappedBy = "collections")
    @OrderBy("name")
    @IndexedEmbedded(includePaths = {"id", "name"})
    private List<Subject> subjects = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "THUMBNAIL_ID")
    private Thumbnail thumbnail;

    @Formula("(select count(*) from TITLE_COL tc where tc.COLLECTION_ID = COL_ID)")
    private long titleCount;

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

    @Override
    public Long getCategoryId() {
        return getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @FullTextField(analyzer = "english")
    @KeywordField(name="name_sort", sortable = Sortable.YES)
    @IndexingDependency(derivedFrom = {
            @ObjectPath(@PropertyValue(propertyName = "name")),
            @ObjectPath(@PropertyValue(propertyName = "parent"))})
    @Override
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        for (Collection c: getCollectionBreadcrumbs()) {
            sb.append(c.getName());
            sb.append(" / ");
        }
        sb.append(getName());
        return sb.toString();
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public String getDescription() {
        return getDisplayComment();
    }

    @Override
    public void setDescription(String description) {
        setDisplayComment(description);
    }

    @Override
    public List<Category> getSubcategories() {
        return new ArrayList<>(getChildren());
    }

    @Override
    public List<Category> getParents() {
        List<Category> parents = new ArrayList<>();
        Collection parent = getParent();
        if (parent != null) parents.add(parent);
        parents.addAll(getSubjects());
        return parents;
    }

    @Override
    public Category getParentCategory() {
        Collection parent = getParent();
        if (parent != null) return parent;
        return null;
    }

    @Override
    public void setParentCategory(Category parent) {
        if (parent instanceof Collection) {
            setParent((Collection) parent);
        } else if (parent instanceof Subject) {
            getSubjects().add((Subject) parent);
        } else {
            throw new IllegalArgumentException(parent.getClass().getName());
        }
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

    @Override
    public String getType() {
        return getClass().getSimpleName();
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
}