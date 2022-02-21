package pandas.collection;

import org.hibernate.annotations.Type;
import pandas.agency.Agency;

import javax.persistence.*;

@Entity
@Table(name = "EMAIL_TEMPLATE")
public class EmailTemplate {
    @Id
    @Column(name = "EMAIL_TEMPLATE_ID", nullable = false, precision = 0)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "AGENCY_ID", nullable = false)
    private Agency agency;

    @ManyToOne
    @JoinColumn(name = "CONTACT_TYPE_ID")
    private ContactType contactType;

    @Column(name = "NAME", nullable = false, length = 256)
    private String name;

    @Column(name = "TEMPLATE", nullable = true, length = 1000000)
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String template;

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

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public ContactType getContactType() {
        return contactType;
    }

    public void setContactType(ContactType contactType) {
        this.contactType = contactType;
    }
}
