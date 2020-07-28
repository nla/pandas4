package pandas.admin.collection;

import org.apache.maven.model.Site;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity(name = "COL")
@DynamicUpdate
@Indexed
public class Collection implements Category {
    @Id
    @Column(name = "COL_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COL_SEQ")
    @SequenceGenerator(name = "COL_SEQ", sequenceName = "COL_SEQ", allocationSize = 1)
    private Long id;

    private String displayComment;
    private Integer displayOrder;
    private boolean isDisplayed;
    private String thumbnailUrl;

    @Field
    private String name;

    @ManyToOne
    @JoinColumn(name = "COL_PARENT_ID")
    private Collection parent;

    @OneToMany(mappedBy = "parent")
    private List<Collection> children = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "TITLE_COL",
            joinColumns = @JoinColumn(name = "COLLECTION_ID"),
            inverseJoinColumns = @JoinColumn(name = "TITLE_ID"))
    @OrderBy("name")
    private List<Title> titles;

    @ManyToMany(mappedBy = "collections")
    @OrderBy("name")
    private List<Subject> subjects = new ArrayList<>();

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
    public Category getParentCategory() {
        Collection parent = getParent();
        if (parent != null) return parent;
        List<Subject> subjects = getSubjects();
        if (!subjects.isEmpty()) return subjects.get(0);
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

    @Field
    @Override
    public String getFullName() {
        var list = new ArrayList<String>();
        for (Category c = this; c != null; c = c.getParentCategory()) {
            list.add(c.getName());
        }
        Collections.reverse(list);
        return String.join(" / ", list);
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
}
