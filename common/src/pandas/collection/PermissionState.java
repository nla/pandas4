package pandas.collection;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "PERMISSION_STATE")
public class PermissionState {
    public static final String GRANTED = "Granted";
    public static final String DENIED = "Denied";
    public static final String IMPOSSIBLE = "Impossible";
    public static final String UNKNOWN = "Unknown";

    public static final Set<String> ALL_NAMES = Set.of(GRANTED, DENIED, IMPOSSIBLE, UNKNOWN);

    @Id
    @Column(name = "PERMISSION_STATE_ID", nullable = false, precision = 0)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "PERMISSION_STATE_SEQ")
    @SequenceGenerator(name = "PERMISSION_STATE_SEQ", sequenceName = "PERMISSION_STATE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "PERMISSION_STATE", nullable = false, length = 256)
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionState that = (PermissionState) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }
}
