package pandas.collection;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "PERMISSION_TYPE")
public class PermissionType {
    public static final String TITLE = "Title Permission";
    public static final String PUBLISHER = "Publisher Permission";

    @Id
    @Column(name = "PERMISSION_TYPE_ID", nullable = false, precision = 0)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "PERMISSION_TYPE_SEQ")
    @SequenceGenerator(name = "PERMISSION_TYPE_SEQ", sequenceName = "PERMISSION_TYPE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "PERMISSION_TYPE", nullable = false, length = 256)
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
        PermissionType that = (PermissionType) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }
}
