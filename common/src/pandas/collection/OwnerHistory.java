package pandas.collection;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import pandas.agency.Agency;
import pandas.agency.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "OWNER_HISTORY")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class OwnerHistory {
    @Id
    @Column(name = "OWNER_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OWNER_HISTORY_SEQ")
    @SequenceGenerator(name = "OWNER_HISTORY_SEQ", sequenceName = "OWNER_HISTORY_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "TITLE_ID")
    @NotNull
    private Title title;

    /**
     * The date on which ownership of the title began for this user.
     */
    @Column(name = "OWNERSHIP_DATE")
    @NotNull
    private Instant date;

    /**
     * Agency who owns or owned the title for this period.
     */
    @ManyToOne
    @JoinColumn(name = "AGENCY_ID")
    private Agency agency;

    /**
     * User who owns or owned the title for this period.
     */
    @ManyToOne
    @JoinColumn(name = "INDIVIDUAL_ID")
    private User user;

    /**
     * Note written by the user who was transferring the title to another user.
     */
    @Column(name = "NOTE", length = 4000)
    private String note;

    /**
     * The individual who transferred ownership of this title.
     */
    @ManyToOne
    @JoinColumn(name = "TRANSFERRER_ID")
    private User transferrer;

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getDate() {
        return this.date;
    }

    public void setDate(Instant ownershipDate) {
        this.date = ownershipDate;
    }

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public User getTransferrer() {
        return transferrer;
    }

    public void setTransferrer(User transferrer) {
        this.transferrer = transferrer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
