package pandas.collection;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.*;

@Entity
@Table(name = "STATUS")
public class Status {
    public static final long SELECTED_ID = 3;

    @Id
    @Column(name = "STATUS_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STATUS_SEQ")
    @SequenceGenerator(name = "STATUS_SEQ", sequenceName = "STATUS_SEQ", allocationSize = 1)
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
