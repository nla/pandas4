package pandas.admin.collection;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import pandas.admin.core.Organisation;

import javax.persistence.*;

@Entity
public class Publisher {
    @Id
    @Column(name="PUBLISHER_ID")
    @GenericField(aggregable = Aggregable.YES)
    private Long id;
    private String localReference;
    private String notes;

    @OneToOne
    @JoinColumn(name = "ORGANISATION_ID")
    private Organisation organisation;

    public String getLocalReference() {
        return localReference;
    }

    public void setLocalReference(String localReference) {
        this.localReference = localReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public String getName() {
        return getOrganisation().getName();
    }
}
