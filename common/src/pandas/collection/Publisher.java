package pandas.collection;

import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import pandas.core.Organisation;
import pandas.core.View;

import javax.persistence.*;
import java.util.Collection;

/**
 * An organisation (which may consist of a single person) that holds the copyright to one or more titles.
 */
@Entity
@Indexed
public class Publisher {
    @Id
    @Column(name = "PUBLISHER_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "PUBLISHER_SEQ")
    @SequenceGenerator(name = "PUBLISHER_SEQ", sequenceName = "PUBLISHER_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    @JsonView(View.Summary.class)
    private Long id;

    /**
     * The local reference number for this publisher. For the NLA, this will be a TRIM file number.
     */
    @Column(name = "LOCAL_REFERENCE", length = 256)
    private String localReference;

    @Column(name = "NOTES", length = 4000)
    private String notes;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ORGANISATION_ID")
    @IndexedEmbedded(includePaths = {"id", "name", "abn"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Organisation organisation;

    @OneToMany(mappedBy = "publisher")
    @OrderBy("name")
    private Collection<Title> titles;

    @ManyToOne
    @JoinColumn(name = "PUBLISHER_TYPE_ID")
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    @JsonView(View.Summary.class)
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
    @JsonView(View.Summary.class)
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
