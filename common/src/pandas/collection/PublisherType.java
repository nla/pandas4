package pandas.collection;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.*;

@Entity
@Table(name = "PUBLISHER_TYPE")
public class PublisherType {
    @Id
    @Column(name = "PUBLISHER_TYPE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PUBLISHER_TYPE_SEQ")
    @SequenceGenerator(name = "PUBLISHER_TYPE_SEQ", sequenceName = "PUBLISHER_TYPE_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    @Column(name = "PUBLISHER_DESCRIPTION")
    private String description;

    @Column(name = "PUBLISHER_TYPE")
    private String name;

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
}
