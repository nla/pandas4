package pandas.admin.collection;

import javax.persistence.*;

@Entity
public class Title {
    @Id
    @Column(name = "TITLE_ID")
    private Long id;

    private Long pi;
    private String name;
    private String titleUrl;

    @OneToOne
    @JoinColumn(name = "TEP_ID")
    private Tep tep;

    public String getName() {
        return name;
    }

    public boolean isVisible() {
        return getTep() != null && getTep().isDoCollection();
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tep getTep() {
        return tep;
    }

    public Long getPi() {
        return pi;
    }

    public void setPi(Long pi) {
        this.pi = pi;
    }

    public String getTitleUrl() {
        return titleUrl;
    }

    public void setTitleUrl(String titleUrl) {
        this.titleUrl = titleUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
