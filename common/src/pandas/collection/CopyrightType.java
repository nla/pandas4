package pandas.collection;

import javax.persistence.*;
import java.util.Objects;

/**
 * Lookup table for the type of copyright/disclaimer URL and note displayed on a title's TEP
 */
@Entity
@Table(name = "COPYRIGHT_TYPE")
public class CopyrightType {
    private Long id;
    private String name;

    @Id
    @GeneratedValue
    @Column(name = "COPYRIGHT_TYPE_ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "COPYRIGHT_TYPE", nullable = false, length = 64)
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
        CopyrightType that = (CopyrightType) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }
}
