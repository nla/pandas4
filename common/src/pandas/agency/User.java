package pandas.agency;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.springframework.security.crypto.bcrypt.BCrypt;
import pandas.core.Individual;
import pandas.core.LinkedAccount;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Entity
public class User extends Individual {

    @Column(name = "AUDIT_CREATE_USERID")
    private Long auditCreateUserid;

    @Column(name = "AUDIT_USERID")
    private Long auditUserid;

    @Column(name = "IS_ACTIVE")
    private Boolean active;

    @Column(name = "PASSWORD")
    @JsonIgnore
    private String password;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LinkedAccount> linkedAccounts;

    public User() {
    }

    public User(Agency agency) {
        setActive(true);
        getRole().setOrganisation(agency.getOrganisation());
        getRole().setType("StdUser");
        getRole().setTitle("Standard User");
    }

    public Long getAuditCreateUserid() {
        return this.auditCreateUserid;
    }

    public void setAuditCreateUserid(Long auditCreateUserid) {
        this.auditCreateUserid = auditCreateUserid;
    }

    public Long getAuditUserid() {
        return this.auditUserid;
    }

    public void setAuditUserid(Long auditUserid) {
        this.auditUserid = auditUserid;
    }

    public boolean isActive() {
        return this.active != null && this.active;
    }

    public void setActive(boolean isActive) {
        this.active = isActive;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        setPwdigest(BCrypt.hashpw(password, BCrypt.gensalt()));
        this.password = password;
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

    public List<LinkedAccount> getLinkedAccounts() {
        return Collections.unmodifiableList(linkedAccounts);
    }
}
