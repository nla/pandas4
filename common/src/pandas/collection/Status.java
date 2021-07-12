package pandas.collection;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "STATUS")
public class Status {
    public static final long NOMINATED_ID = 1;
    public static final long REJECTED_ID = 2;
    public static final long SELECTED_ID = 3;
    public static final long MONITORED_ID = 4;
    public static final long PERMISSION_REQUESTED_ID = 5;
    public static final long PERMISSION_DENIED_ID = 6;
    public static final long PERMISSION_GRANTED_ID = 7;
    public static final long PERMISSION_IMPOSSIBLE_ID = 8;
    public static final long CEASED_ID = 11;

    public static Map<Long,List<Long>> allowedTransitions = Map.of(
            NOMINATED_ID, List.of(SELECTED_ID, MONITORED_ID, REJECTED_ID),
            MONITORED_ID, List.of(SELECTED_ID, REJECTED_ID),
            REJECTED_ID, List.of(NOMINATED_ID, SELECTED_ID),
            SELECTED_ID, List.of(REJECTED_ID, CEASED_ID),
            CEASED_ID, List.of(SELECTED_ID)
    );

    @Id
    @Column(name = "STATUS_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STATUS_SEQ")
    @SequenceGenerator(name = "STATUS_SEQ", sequenceName = "STATUS_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    @Column(name = "STATUS_NAME")
    private String name;

    public boolean isTransitionAllowed(Status newStatus) {
        return allowedTransitions.getOrDefault(getId(), Collections.emptyList()).contains(newStatus.getId());
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Status status = (Status) o;
        return Objects.equals(id, status.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isCeased() {
        return id != null && id.equals(CEASED_ID);
    }
}
