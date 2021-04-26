package pandas.collection;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "TITLE_PREVIOUS_NAME")
public class TitlePreviousName {
    /**
     * The date the previous name stopped being current.
     */
    @Basic
    @Column(name = "DATE_CHANGED", nullable = false)
    private Instant date;

    /**
     * The name an "integrating" format title was previously known as.
     */
    @Basic
    @Column(name = "PREVIOUS_NAME", nullable = false, length = 256)
    private String name;

    @Id
    @Column(name = "TITLE_HISTORY_ID", nullable = false, precision = 0)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "TITLE_ID", nullable = false)
    private Title title;

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String previousName) {
        this.name = previousName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }
}
