package pandas.collection;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

@Entity
@Table(name = "STATUS")
@Immutable
public class StatusEntity {
    @Id
    @Column(name = "STATUS_ID")
    @GenericField(aggregable = Aggregable.YES)
    private Integer id;

    @Column(name = "STATUS_NAME")
    private String name;

    protected StatusEntity() {
    }

    public StatusEntity(Status status) {
        id = status.id();
        name = status.name();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
