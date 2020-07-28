package pandas.admin.collection;

import javax.persistence.*;

@Entity
public class Title implements Site {
    @Id
    @Column(name = "TITLE_ID")
    private Long id;

    private String name;

    @OneToOne
    @JoinColumn(name = "TEP_ID")
    private Tep tep;

    public String getName() {
        return name;
    }

    @Override
    public boolean isVisible() {
        return getTep() != null && getTep().isDoCollection();
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tep getTep() {
        return tep;
    }
}
