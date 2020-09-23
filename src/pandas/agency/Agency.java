package pandas.agency;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import pandas.core.Organisation;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "AGENCY")
public class Agency {
    @Id
    @Column(name = "AGENCY_ID")
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    @Column(name = "EXTERNAL_EMAIL")
    private String externalEmail;

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

    @OneToOne
    @JoinColumn(name = "ORGANISATION_ID")
    private Organisation organisation;

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
    }
}
