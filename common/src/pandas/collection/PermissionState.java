package pandas.collection;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "PERMISSION_STATE")
public class PermissionState {
    @Id
    @GeneratedValue
    @Column(name = "PERMISSION_STATE_ID", nullable = false, precision = 0)
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
