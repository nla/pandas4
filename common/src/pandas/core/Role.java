package pandas.core;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "ROLE", indexes = @Index(name = "role_individual_id_index", columnList = "individual_id"))
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Role {
    public static final String TYPE_SYSADMIN = "SysAdmin";
    public static final String TYPE_CONTACT = "contact";
    public static final Map<String, String> titles = initTitlesMap();

    private static Map<String, String> initTitlesMap() {
        var map = new LinkedHashMap<String, String>();
        map.put("SuppUser", "Support User");
        map.put("InfoUser", "Informational User");
        map.put("StdUser", "Standard User");
        map.put("AgAdmin", "Agency Administrator");
        map.put("PanAdmin", "Pandas Administrator");
        map.put(TYPE_SYSADMIN, "System Administrator");
        map.put(TYPE_CONTACT, "Publisher Contact");
        return Collections.unmodifiableMap(map);
    }

    @Id
    @Column(name = "ROLE_ID")
    @UseIdentityGeneratorIfMySQL
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ROLE_SEQ")
    @SequenceGenerator(name = "ROLE_SEQ", sequenceName = "ROLE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "AUDIT_CREATE_DATE")
    private Instant auditCreateDate;

    @Column(name = "AUDIT_DATE")
    private Instant auditDate;

    @Column(name = "AUDIT_USERID")
    private Long auditUserid;

    @Column(name = "COMMENTS")
    private String comments;

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
