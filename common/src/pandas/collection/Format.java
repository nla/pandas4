package pandas.collection;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import jakarta.persistence.*;

@Entity
@Table(name = "FORMAT")
public class Format {
    public static final long INTEGRATING_ID = 3;
    public static final long DEFAULT_ID = INTEGRATING_ID;

    @Id
    @Column(name = "FORMAT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FORMAT_SEQ")
    @SequenceGenerator(name = "FORMAT_SEQ", sequenceName = "FORMAT_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    @Column(name = "NAME")
    private String name;

    public Format() {
    }

    public Format(String name) {
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long formatId) {
        this.id = formatId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
