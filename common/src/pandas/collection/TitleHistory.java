package pandas.collection;

import javax.persistence.*;
import java.time.Instant;

/**
 * A relationship between two "serial" format titles where one has replaced the other.
 */
@Entity
@Table(name = "TITLE_HISTORY")
public class TitleHistory {
    @Id
    @Column(name = "TITLE_HISTORY_ID", nullable = false, precision = 0)
    private Long id;

    /**
     * The title which has been replaced by a new (continuing) title.
     */
    @ManyToOne
    @JoinColumn(name = "CEASED_ID", nullable = false)
    private Title ceased;

    /**
     * The title which took over from a ceased title.
     */
    @ManyToOne
    @JoinColumn(name = "CONTINUES_ID", nullable = false)
    private Title continues;

    /**
     * The date the old title was replaced by the new title.
     */
    @Column(name = "DATE_CHANGED", nullable = false)
    private Instant date;

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Title getCeased() {
        return ceased;
    }

    public void setCeased(Title ceased) {
        this.ceased = ceased;
    }

    public Title getContinues() {
        return continues;
    }

    public void setContinues(Title continues) {
        this.continues = continues;
    }
}
