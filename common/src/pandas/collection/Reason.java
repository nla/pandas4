package pandas.collection;

import jakarta.persistence.*;

@Entity
public class Reason {
    @Id
    @Column(name = "REASON_ID", nullable = false)
    private Long id;

    @Column(name = "STATUS_ID")
    private Status status;

    @Column(name = "REASON", nullable = false, length = 512)
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
