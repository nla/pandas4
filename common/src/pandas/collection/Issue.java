package pandas.collection;

import pandas.gather.Instance;

import javax.persistence.*;

/**
 * An alternate entry point into a archived instance. This is published in the display system in addition to the
 * instance's main entry URL.
 */
@Entity
@Table(name = "ARCH_ISSUE",
        indexes = {@Index(name = "issue_issue_group_id_order_index", columnList = "issue_group_id, issue_order")})
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ARCH_ISSUE_SEQ")
    @SequenceGenerator(name = "ARCH_ISSUE_SEQ", sequenceName = "ARCH_ISSUE_SEQ", allocationSize = 1)
    @Column(name = "ISSUE_ID", nullable = false, precision = 0)
    private Long id;

    @Basic
    @Column(name = "IS_DISPLAYED", nullable = true, precision = 0)
    private Boolean isDisplayed = true;

    @Basic
    @Column(name = "ISSUE_ORDER", nullable = true, precision = 0)
    private Integer order;

    @Column(name = "TITLE", nullable = true, length = 1024)
    private String name;

    @Column(name = "URL", nullable = true, length = 1024)
    private String url;

    @ManyToOne
    @JoinColumn(name = "ISSUE_GROUP_ID", referencedColumnName = "ISSUE_GROUP_ID")
    private IssueGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
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

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
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
