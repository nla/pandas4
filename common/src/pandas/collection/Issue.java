package pandas.collection;

import pandas.gather.Instance;

import javax.persistence.*;

/**
 * An alternate entry point into a archived instance. This is published in the display system in addition to the
 * instance's main entry URL.
 */
@Entity
@Table(name = "ARCH_ISSUE")
public class Issue {
    @Id
    @GeneratedValue
    @Column(name = "ISSUE_ID", nullable = false, precision = 0)
    private Long id;

    @Basic
    @Column(name = "IS_DISPLAYED", nullable = true, precision = 0)
    private Boolean isDisplayed;

    @Basic
    @Column(name = "ISSUE_ORDER", nullable = true, precision = 0)
    private Long order;

    @Column(name = "TITLE", nullable = true, length = 1024)
    private String name;

    @Column(name = "URL", nullable = true, length = 1024)
    private String url;

    @ManyToOne
    @JoinColumn(name = "ISSUE_GROUP_ID", referencedColumnName = "ISSUE_GROUP_ID")
    private IssueGroup group;

    @ManyToOne
    @JoinColumn(name = "INSTANCE_ID")
    private Instance instance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getDisplayed() {
        return isDisplayed;
    }

    public void setDisplayed(Boolean displayed) {
        isDisplayed = displayed;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String title) {
        this.name = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public IssueGroup getGroup() {
        return group;
    }

    public void setGroup(IssueGroup group) {
        this.group = group;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}
