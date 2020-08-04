package pandas.admin.collection;

import javax.persistence.*;

@Entity
@Table(name = "STATUS")
public class Status {
    @Id
    @Column(name = "STATUS_ID")
    private Long id;

    @Column(name = "STATUS_NAME")
    private String name;

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
}
