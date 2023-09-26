package pandas.core;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import pandas.collection.Publisher;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "INDIVIDUAL")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@DiscriminatorFormula("case when USERID is not null then 'User' else 'ContactPerson' end")
public abstract class Individual {
    @Id
    @Column(name = "INDIVIDUAL_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "INDIVIDUAL_SEQ")
    @SequenceGenerator(name = "INDIVIDUAL_SEQ", sequenceName = "INDIVIDUAL_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    private Long id;
    @Column(name = "AUDIT_CREATE_DATE")
    private Instant auditCreateDate;
    @Column(name = "AUDIT_DATE")
    private Instant auditDate;
    @Column(name = "EMAIL")
    private String email;
    @Column(name = "FAX")
    private String fax;
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
    @Column(name = "PHONE")
    private String phone;
    @Column(name = "URL")
    private String url;
    @OneToOne(mappedBy = "individual", cascade = CascadeType.ALL)
    private Role role;

    public String getFullName() {
        return getNameGiven() + " " + getNameFamily();
    }

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

    @JsonView(View.Summary.class)
    public Long getId() {
        return this.id;
    }

    public void setId(Long individualId) {
        this.id = individualId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Individual that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
