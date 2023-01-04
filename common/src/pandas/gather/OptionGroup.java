package pandas.gather;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "OPTION_GROUP")
public class OptionGroup {
    private Long accessLevel;
    private Long id;
    private Long displayOrder;
    private String name;

    @Basic
    @Column(name = "ACCESS_LEVEL", nullable = true, precision = 0)
    public Long getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Long accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Id
    @Column(name = "OPTION_GROUP_ID", nullable = false, precision = 0)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "DISPLAY_ORDER", nullable = true, precision = 0)
    public Long getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Long displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Basic
    @Column(name = "GROUP_NAME", nullable = true, length = 256)
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
        OptionGroup that = (OptionGroup) o;
        return Objects.equals(accessLevel, that.accessLevel) && Objects.equals(id, that.id) && Objects.equals(displayOrder, that.displayOrder) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessLevel, id, displayOrder, name);
    }
}
