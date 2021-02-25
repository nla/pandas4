package pandas.collection;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import org.springframework.stereotype.Indexed;
import pandas.core.Organisation;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Indexed
public class Publisher {
    @Id
    @Column(name="PUBLISHER_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "PUBLISHER_SEQ")
    @SequenceGenerator(name = "PUBLISHER_SEQ", sequenceName = "PUBLISHER_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    private Long id;
    private String localReference;
    private String notes;

    @OneToOne
    @JoinColumn(name = "ORGANISATION_ID")
    @IndexedEmbedded(includePaths = {"id", "name"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Organisation organisation;

    @OneToMany(mappedBy = "publisher")
    private Collection<Title> titles;

    @ManyToOne
    @JoinColumn(name = "PUBLISHER_TYPE_ID")
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private PublisherType type;

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

    @FullTextField(analyzer = "english")
    @KeywordField(name = "name_sort", sortable = Sortable.YES)
    @IndexingDependency(derivedFrom = {
            @ObjectPath(@PropertyValue(propertyName = "organisation"))})
    public String getName() {
        return getOrganisation().getName();
    }

    public PublisherType getType() {
        return type;
    }

    public void setType(PublisherType type) {
        this.type = type;
    }

    public Collection<Title> getTitles() {
        return titles;
    }

    public void setTitles(Collection<Title> titles) {
        this.titles = titles;
    }
}
