package pandas.core;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ROLE")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Role {
    @Column(name = "AUDIT_CREATE_DATE")
    private Instant auditCreateDate;

    @Column(name = "AUDIT_DATE")
    private Instant auditDate;

    @Column(name = "AUDIT_USERID")
    private Long auditUserid;

    @Column(name = "COMMENTS")
    private String comments;

    @Id
    @Column(name = "ROLE_ID")
    private Long id;

    @Column(name = "ROLE_TITLE")
    private String title;

    @Column(name = "ROLE_TYPE")
    private String type;

    @OneToOne
    @JoinColumn(name = "INDIVIDUAL_ID")
    private Individual individual;

    @ManyToOne
    @JoinColumn(name = "ORGANISATION_ID")
    private Organisation organisation;

    public Instant getAuditCreateDate() {
        return this.auditCreateDate;
    }

    public void setAuditCreateDate(Instant auditCreateDate) {
        this.auditCreateDate = auditCreateDate;
    }

    public Instant getAuditDate() {
        return this.auditDate;
    }

    public void setAuditDate(Instant auditDate) {
        this.auditDate = auditDate;
    }

    public Long getAuditUserid() {
        return this.auditUserid;
    }

    public void setAuditUserid(Long auditUserid) {
        this.auditUserid = auditUserid;
    }

    public String getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long roleId) {
        this.id = roleId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String roleTitle) {
        this.title = roleTitle;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String roleType) {
        this.type = roleType;
    }

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}
