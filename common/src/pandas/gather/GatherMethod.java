package pandas.gather;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.*;

@Entity
@Table(name = "GATHER_METHOD")
public class GatherMethod {
    public static final String HTTRACK = "HTTrack";
    public static final String UPLOAD = "Upload";
    public static final String HERITRIX = "Heritrix";

    @Id
    @Column(name = "GATHER_METHOD_ID")
    @GenericField(aggregable = Aggregable.YES)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GATHER_METHOD_SEQ")
    @SequenceGenerator(name = "GATHER_METHOD_SEQ", sequenceName = "GATHER_METHOD_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "METHOD_DESC")
    private String description;

    @Column(name = "METHOD_NAME")
    private String name;


    public Long getId() {
        return this.id;
    }

    public void setId(Long gatherMethodId) {
        this.id = gatherMethodId;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String methodDesc) {
        this.description = methodDesc;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String methodName) {
        this.name = methodName;
    }
}
