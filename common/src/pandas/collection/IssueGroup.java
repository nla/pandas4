package pandas.collection;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A label under which to group issues on the title's TEP.
 */
@Entity
@Table(name = "ISSUE_GROUP",
        indexes = {@Index(name = "issue_group_tep_id_order_index", columnList = "tep_id, issue_group_order")})
public class IssueGroup {
    public static final String NONE_GROUP_NAME = "-None-";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ISSUE_GROUP_SEQ")
    @SequenceGenerator(name = "ISSUE_GROUP_SEQ", sequenceName = "ISSUE_GROUP_SEQ", allocationSize = 1)
    @Column(name = "ISSUE_GROUP_ID", nullable = false, precision = 0)
    private Long id;

    @Column(name = "NAME", nullable = true, length = 256)
    private String name;

    @Column(name = "NOTES", nullable = true, length = 4000)
    private String notes;

    @Column(name = "ISSUE_GROUP_ORDER", nullable = true, precision = 0)
    private Integer order;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("order")
    private Collection<Issue> issues = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEP_ID", referencedColumnName = "TEP_ID")
    private Tep tep;

    public static IssueGroup newNoneGroup() {
        IssueGroup group = new IssueGroup();
        group.setName(NONE_GROUP_NAME);
        return group;
    }

    public boolean isNone() {
        return NONE_GROUP_NAME.equals(getName());
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Collection<Issue> getIssues() {
        return Collections.unmodifiableCollection(issues);
    }

    public void removeAllIssues() {
        issues.clear();
    }

    public void addIssue(Issue issue) {
        issues.add(issue);
        issue.setGroup(this);
    }

    public Tep getTep() {
        return tep;
    }

    public void setTep(Tep tep) {
        this.tep = tep;
    }
}
