package pandas.collection;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "FORMAT")
public class Format {
    public static final long INTEGRATING_ID = 3;
    @Id
    @Column(name = "FORMAT_ID")
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    @Column(name = "NAME")
    private String name;

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
