package pandas.core;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.springframework.security.crypto.bcrypt.BCrypt;
import pandas.agency.Agency;

import javax.persistence.*;
import java.time.Instant;
import java.time.ZoneId;

@Entity
@Table(name = "INDIVIDUAL")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Individual {
    @Id
    @Column(name = "INDIVIDUAL_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "INDIVIDUAL_SEQ")
    @SequenceGenerator(name = "INDIVIDUAL_SEQ", sequenceName = "INDIVIDUAL_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    @Column(name = "AUDIT_CREATE_DATE")
    private Instant auditCreateDate;

    @Column(name = "AUDIT_CREATE_USERID")
    private Long auditCreateUserid;

    @Column(name = "AUDIT_DATE")
    private Instant auditDate;

    @Column(name = "AUDIT_USERID")
    private Long auditUserid;

    @Column(name = "COMMENTS")
    private String comments;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "FAX")
    private String fax;

    @Column(name = "FUNCTION")
    private String function;

    @Column(name = "IS_ACTIVE")
    private Boolean active;

    @Column(name = "MOBILE_PHONE")
    private String mobilePhone;

    @Column(name = "NAME_FAMILY")
    @FullTextField(analyzer = "english")
    private String nameFamily;

    @Column(name = "NAME_GIVEN")
    @FullTextField(analyzer = "english")
    private String nameGiven;

    @Column(name = "NAME_TITLE")
    private String nameTitle;

    @Column(name = "PASSWORD")
    @JsonIgnore
    private String password;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "URL")
    private String url;

    @Column(name = "USERID", unique = true)
    @GenericField
    private String userid;

    @Column(name = "ALT_CONTACT_ID")
    private Long altContactId;

    @Column(name = "EMAIL_SIGNATURE")
    private String emailSignature;

    @Column(name = "PWDIGEST")
    @JsonIgnore
    private String pwdigest;

    @OneToOne(mappedBy = "individual", cascade = CascadeType.ALL)
    private Role role;

    public Individual() {
    }

    public Individual(Agency agency) {
        setActive(true);
        getRole().setOrganisation(agency.getOrganisation());
        getRole().setType("StdUser");
        getRole().setTitle("Standard User");
    }

    public String getFullName() {
        return getNameGiven() + " "  + getNameFamily();
    }

    public Instant getAuditCreateDate() {
        return this.auditCreateDate;
    }

    public void setAuditCreateDate(Instant auditCreateDate) {
        this.auditCreateDate = auditCreateDate;
    }

    public Long getAuditCreateUserid() {
        return this.auditCreateUserid;
    }

    public void setAuditCreateUserid(Long auditCreateUserid) {
        this.auditCreateUserid = auditCreateUserid;
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

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return this.fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getFunction() {
        return this.function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long individualId) {
        this.id = individualId;
    }

    public boolean isActive() {
        return this.active != null && this.active;
    }

    public void setActive(boolean isActive) {
        this.active = isActive;
    }

    public String getMobilePhone() {
        return this.mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getNameFamily() {
        return this.nameFamily;
    }

    public void setNameFamily(String nameFamily) {
        this.nameFamily = nameFamily;
    }

    public String getNameGiven() {
        return this.nameGiven;
    }

    public void setNameGiven(String nameGiven) {
        this.nameGiven = nameGiven;
    }

    public String getNameTitle() {
        return this.nameTitle;
    }

    public void setNameTitle(String nameTitle) {
        this.nameTitle = nameTitle;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        setPwdigest(BCrypt.hashpw(password, BCrypt.gensalt()));
        this.password = password;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserid() {
        return this.userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Long getAltContactId() {
        return this.altContactId;
    }

    public void setAltContactId(Long altContactId) {
        this.altContactId = altContactId;
    }

    public String getEmailSignature() {
        return this.emailSignature;
    }

    public void setEmailSignature(String emailSignature) {
        this.emailSignature = emailSignature;
    }

    public String getPwdigest() {
        return this.pwdigest;
    }

    public void setPwdigest(String pwdigest) {
        this.pwdigest = pwdigest;
    }

    public String getName() {
        return nameGiven + " " + nameFamily;
    }

    public Role getRole() {
        if (role == null) {
            setRole(new Role());
        }
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
        role.setIndividual(this);
    }

    public Agency getAgency() {
        if (getRole() == null) return null;
        if (getRole().getOrganisation() == null) return null;
        return getRole().getOrganisation().getAgency();
    }

    public Integer getYearCreated() {
        if (getAuditCreateDate() == null) {
            return null;
        }
        return getAuditCreateDate().atZone(ZoneId.systemDefault()).getYear();
    }
}
