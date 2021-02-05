package pandas.core;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.*;

@Entity
@Table(name = "INDIVIDUAL")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Individual {
    @Column(name = "AUDIT_CREATE_DATE")
    private java.sql.Timestamp auditCreateDate;

    @Column(name = "AUDIT_CREATE_USERID")
    private Long auditCreateUserid;

    @Column(name = "AUDIT_DATE")
    private java.sql.Timestamp auditDate;

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

    @Id
    @Column(name = "INDIVIDUAL_ID")
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    @Column(name = "IS_ACTIVE")
    private Long isActive;

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

    @Column(name = "USERID")
    @GenericField
    private String userid;

    @Column(name = "ALT_CONTACT_ID")
    private Long altContactId;

    @Column(name = "EMAIL_SIGNATURE")
    private String emailSignature;

    @Column(name = "PWDIGEST")
    @JsonIgnore
    private String pwdigest;

    @OneToOne(mappedBy = "individual")
    private Role role;

    public String getFullName() {
        return getNameGiven() + " "  + getNameFamily();
    }

    public java.sql.Timestamp getAuditCreateDate() {
        return this.auditCreateDate;
    }

    public void setAuditCreateDate(java.sql.Timestamp auditCreateDate) {
        this.auditCreateDate = auditCreateDate;
    }

    public Long getAuditCreateUserid() {
        return this.auditCreateUserid;
    }

    public void setAuditCreateUserid(Long auditCreateUserid) {
        this.auditCreateUserid = auditCreateUserid;
    }

    public java.sql.Timestamp getAuditDate() {
        return this.auditDate;
    }

    public void setAuditDate(java.sql.Timestamp auditDate) {
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

    public Long getIsActive() {
        return this.isActive;
    }

    public void setIsActive(Long isActive) {
        this.isActive = isActive;
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
        return role;
    }
}
