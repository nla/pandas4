package pandas.admin.core;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Organisation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ORGANISATION_SEQ")
    @SequenceGenerator(name = "ORGANISATION_SEQ", sequenceName = "ORGANISATION_SEQ", allocationSize = 1)
    private Long organisationId;

    private String alias;
    private Long agencyId;
    private java.sql.Timestamp auditDate;
    private Long auditUserid;
    private String comments;
    private String longcountry;
    private String email;
    private String fax;
    private Long indexerId;
    private String line1;
    private String line2;
    private String locality;
    private String mobilePhone;
    private String name;
    private String phone;
    private String postcode;
    private Long publisherId;
    private Long serviceId;
    private String longstate;
    private String url;

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Long getAgencyId() {
        return this.agencyId;
    }

    public void setAgencyId(Long agencyId) {
        this.agencyId = agencyId;
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

    public String getLongcountry() {
        return this.longcountry;
    }

    public void setLongcountry(String longcountry) {
        this.longcountry = longcountry;
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

    public Long getIndexerId() {
        return this.indexerId;
    }

    public void setIndexerId(Long indexerId) {
        this.indexerId = indexerId;
    }

    public String getLine1() {
        return this.line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return this.line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getLocality() {
        return this.locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getMobilePhone() {
        return this.mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOrganisationId() {
        return this.organisationId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public Long getPublisherId() {
        return this.publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public Long getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getLongstate() {
        return this.longstate;
    }

    public void setLongstate(String longstate) {
        this.longstate = longstate;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
