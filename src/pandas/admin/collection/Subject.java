package pandas.admin.collection;

import org.apache.maven.model.Site;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.springframework.web.bind.annotation.PathVariable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Entity
@DynamicUpdate
@Indexed
public class Subject implements Category {
    public static final long CATEGORY_ID_RANGE_START = 15000;
    public static final long CATEGORY_ID_RANGE_END = 15999;

    @Id
    @Column(name = "SUBJECT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SUBJECT_SEQ")
    @SequenceGenerator(name = "SUBJECT_SEQ", sequenceName = "SUBJECT_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "SUBJECT_NAME")
    @Field
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
    @JoinTable(name = "COL_SUBS",
            joinColumns = @JoinColumn(name = "SUBJECT_ID"),
            inverseJoinColumns = @JoinColumn(name = "COL_ID"))
    @OrderBy("name")
    List<Collection> collections;

    static boolean isInRange(@PathVariable("id") long id) {
        return id >= CATEGORY_ID_RANGE_START && id <= CATEGORY_ID_RANGE_END;
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
    public List<Title> getTitles() {
        return List.of();
    }

    @Override
    public String getType() {
        return getClass().getSimpleName();
    }

    @Field
    @Override
    public String getFullName() {
        var list = new ArrayList<String>();
        for (Subject s = this; s != null; s = s.getParent()) {
            list.add(s.getName());
        }
        Collections.reverse(list);
        return String.join(" / ", list);
    }

    public List<Subject> getChildren() {
        return children;
    }

    public List<Collection> getCollections() {
        return collections;
    }

}
