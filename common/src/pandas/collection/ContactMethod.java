package pandas.collection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CONTACT_METHOD")
public class ContactMethod {
    @Id
    @Column(name = "CONTACT_METHOD_ID", nullable = false, precision = 0)
    private Long id;

    @Column(name = "CONTACT_METHOD", nullable = false, length = 64)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
