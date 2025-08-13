package pandas.collection;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import pandas.agency.Agency;
import pandas.agency.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

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

    public OwnerHistory() {
    }

    public OwnerHistory(Title title, Agency agency, User owner, String note, User transferrer, Instant date) {
        this.title = title;
        this.agency = agency;
        this.user = owner;
        this.note = note;
        this.transferrer = transferrer;
        this.date = date;
    }

    public String getNote() {
        return this.note;
    }

    public Instant getDate() {
        return this.date;
    }

    public Agency getAgency() {
        return agency;
    }

    public User getUser() {
        return user;
    }

    public Title getTitle() {
        return title;
    }

    public User getTransferrer() {
        return transferrer;
    }

    public Long getId() {
        return id;
    }
}
