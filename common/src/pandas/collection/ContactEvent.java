package pandas.collection;

import pandas.agency.User;

import javax.persistence.*;
import java.time.Instant;

/**
 * A communication (contact event) between a PANDAS user and a contact person for a publishing agency or title.
 */
@Entity
@Table(name = "CONTACT")
public class ContactEvent {
    @Id
    @Column(name = "CONTACT_ID", nullable = false, precision = 0)
    private Long id;

    /**
     * The date and time on which a communication occurred
     */
    @Column(name = "CONTACT_DATE", nullable = true)
    private Instant date;

    /**
     * Indexing agency which this communication was with.
     */
    @Column(name = "INDEXER_ID", nullable = true, precision = 0)
    private Long indexerId;

    /**
     * The contact person this communication was with. This person should be associated with a publisher or an indexing
     * agency.
     */
    @ManyToOne
    @JoinColumn(name = "INDIVIDUAL_ID", nullable = false)
    private ContactPerson contactPerson;

    /**
     * A note on what this communication was about
     */
    @Column(name = "NOTE", nullable = true, length = 4000)
    private String note;

    /**
     * The publisher which this communication was with.
     */
    @ManyToOne
    @JoinColumn(name = "PUBLISHER_ID", nullable = true)
    private Publisher publisher;

    /**
     * The title this communication was in reference to.
     */
    @ManyToOne
    @JoinColumn(name = "TITLE_ID", nullable = true)
    private Title title;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "CONTACT_METHOD_ID", referencedColumnName = "CONTACT_METHOD_ID")
    private ContactMethod method;

    @ManyToOne
    @JoinColumn(name = "CONTACT_TYPE_ID", referencedColumnName = "CONTACT_TYPE_ID")
    private ContactType type;

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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public ContactMethod getMethod() {
        return method;
    }

    public void setMethod(ContactMethod method) {
        this.method = method;
    }

    public ContactType getType() {
        return type;
    }

    public void setType(ContactType type) {
        this.type = type;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public ContactPerson getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(ContactPerson contactPerson) {
        this.contactPerson = contactPerson;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
