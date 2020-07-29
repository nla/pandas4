package pandas.admin.collection;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Indexed
public class Title {
    @Id
    @Column(name = "TITLE_ID")
    private Long id;

    @Field
    private Long pi;

    @Field
    private String name;

    @Field
    private String titleUrl;
    private LocalDateTime regDate;

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

    public LocalDateTime getRegDate() {
        return regDate;
    }

    public void setRegDate(LocalDateTime regDate) {
        this.regDate = regDate;
    }
}
