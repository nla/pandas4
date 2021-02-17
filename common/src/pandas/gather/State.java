package pandas.gather;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "STATE")
public class State {
    @Id
    @Column(name = "STATE_ID")
    private Long id;

    @Column(name = "STATE_NAME")
    private String name;


    public Long getId() {
        return this.id;
    }

    public void setId(Long stateId) {
        this.id = stateId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String stateName) {
        this.name = stateName;
    }
}
