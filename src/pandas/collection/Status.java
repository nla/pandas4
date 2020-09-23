package pandas.collection;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "STATUS")
public class Status {
    @Id
    @Column(name = "STATUS_ID")
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    @Column(name = "STATUS_NAME")
    private String name;

    public Long getId() {
        return this.id;
    }

    public void setId(Long statusId) {
        this.id = statusId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String statusName) {
        this.name = statusName;
    }
}
