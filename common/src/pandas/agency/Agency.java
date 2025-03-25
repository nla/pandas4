package pandas.agency;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import pandas.core.Organisation;

import java.util.Collection;
import java.util.Objects;

/**
 * A partner agency who is involved in selecting and archiving titles.
 */
@Entity
@Table(name = "AGENCY")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Agency {
    public static final long PADI_ID = 3;

    @Id
    @Column(name = "AGENCY_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "AGENCY_SEQ")
    @SequenceGenerator(name = "AGENCY_SEQ", sequenceName = "AGENCY_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    /**
     * Email address for this agency, to be displayed to the public.
     */
    @Column(name = "EXTERNAL_EMAIL")
    private String externalEmail;

    /**
     * The URL this agency uses to access any form letters they wish to use to communicate with publisher and title
     * contacts.
     */
    @Column(name = "FORM_LETTER_URL")
    private String formLetterUrl;

    @Column(name = "LOCAL_DATABASE_PREFIX")
    private String localDatabasePrefix;

    @Column(name = "LOCAL_REFERENCE_PREFIX")
    private String localReferencePrefix;

    @Column(name = "LEGAL_DEPOSIT")
    private Long legalDeposit;

    @OneToMany(mappedBy = "agency")
    private Collection<AgencyArea> areas;

    /**
     * The organisation which corresponds to this agency.
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ORGANISATION_ID")
    private Organisation organisation = new Organisation();

    /**
     * This agency's logo image file.
     */
    @Column(name = "LOGO")
    @Basic(fetch = FetchType.LAZY)
    private byte[] logo;

    @ManyToOne
    @JoinColumn(name = "TRANSFER_CONTACT_ID")
    private User transferContact;

    public String getName() {
        return getOrganisation().getName();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalEmail() {
        return this.externalEmail;
    }

    public void setExternalEmail(String externalEmail) {
        this.externalEmail = externalEmail;
    }

    public String getFormLetterUrl() {
        return this.formLetterUrl;
    }

    public void setFormLetterUrl(String formLetterUrl) {
        this.formLetterUrl = formLetterUrl;
    }

    public String getLocalDatabasePrefix() {
        return this.localDatabasePrefix;
    }

    public void setLocalDatabasePrefix(String localDatabasePrefix) {
        this.localDatabasePrefix = localDatabasePrefix;
    }

    public String getLocalReferencePrefix() {
        return this.localReferencePrefix;
    }

    public void setLocalReferencePrefix(String localReferencePrefix) {
        this.localReferencePrefix = localReferencePrefix;
    }

    public Long getLegalDeposit() {
        return this.legalDeposit;
    }

    public void setLegalDeposit(Long legalDeposit) {
        this.legalDeposit = legalDeposit;
    }

    public Collection<AgencyArea> getAreas() {
        return areas;
    }

    public void setAreas(Collection<AgencyArea> areas) {
        this.areas = areas;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
        organisation.setAgency(this);
    }

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agency agency = (Agency) o;
        return Objects.equals(id, agency.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public User getTransferContact() {
        return transferContact;
    }

    public void setTransferContact(User transferContact) {
        this.transferContact = transferContact;
    }
}
