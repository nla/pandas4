package pandas.collection;

import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import pandas.core.View;

import jakarta.persistence.*;

@Entity
@Table(name = "PUBLISHER_TYPE")
public class PublisherType {
    @Id
    @Column(name = "PUBLISHER_TYPE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PUBLISHER_TYPE_SEQ")
    @SequenceGenerator(name = "PUBLISHER_TYPE_SEQ", sequenceName = "PUBLISHER_TYPE_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    @JsonView(View.Summary.class)
    private Long id;

    @Column(name = "PUBLISHER_DESCRIPTION")
    private String description;

    @Column(name = "PUBLISHER_TYPE")
    @JsonView(View.Summary.class)
    private String name;

    @Column(name = "DOMAIN_SUFFIXES")
    private String domainSuffixes;

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String publisherDescription) {
        this.description = publisherDescription;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String publisherType) {
        this.name = publisherType;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long publisherTypeId) {
        this.id = publisherTypeId;
    }

    public String getDomainSuffixes() {
        return domainSuffixes;
    }

    public void setDomainSuffixes(String domainSuffixes) {
        this.domainSuffixes = domainSuffixes;
    }
}
