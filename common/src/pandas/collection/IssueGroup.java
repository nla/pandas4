package pandas.collection;

import javax.persistence.*;
import java.util.Collection;

/**
 * A label under which to group issues on the title's TEP.
 */
@Entity
@Table(name = "ISSUE_GROUP")
public class IssueGroup {
    @Id
    @GeneratedValue
    @Column(name = "ISSUE_GROUP_ID", nullable = false, precision = 0)
    private Long id;

    @Column(name = "NAME", nullable = true, length = 256)
    private String name;

    @Column(name = "NOTES", nullable = true, length = 4000)
    private String notes;

    @Column(name = "ISSUE_GROUP_ORDER", nullable = true, precision = 0)
    private Long order;

    @OneToMany(mappedBy = "group")
    private Collection<Issue> issues;

    @ManyToOne
    @JoinColumn(name = "TEP_ID", referencedColumnName = "TEP_ID")
    private Tep tep;

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

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public Collection<Issue> getIssues() {
        return issues;
    }

    public void setIssues(Collection<Issue> issues) {
        this.issues = issues;
    }

    public Tep getTep() {
        return tep;
    }

    public void setTep(Tep tep) {
        this.tep = tep;
    }
}
