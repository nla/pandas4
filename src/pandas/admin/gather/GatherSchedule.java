package pandas.admin.gather;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.*;

@Entity
@Table(name = "GATHER_SCHEDULE")
public class GatherSchedule {
    @Id
    @Column(name = "GATHER_SCHEDULE_ID")
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    @Column(name = "SCHEDULE_NAME")
    private String name;


    public Long getId() {
        return this.id;
    }

    public void setId(Long gatherScheduleId) {
        this.id = gatherScheduleId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String scheduleName) {
        this.name = scheduleName;
    }
}
