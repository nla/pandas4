package pandas.collection;

import jakarta.persistence.*;
import pandas.core.Individual;

import java.time.Instant;

/**
 * Information about whether the publisher of a title has granted or denied access to archived versions of that title,
 * or to a group of related titles which they have the rights to.
 */
@Entity
public class Permission {
    @Id
    @Column(name = "PERMISSION_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "PERMISSION_SEQ")
    @SequenceGenerator(name = "PERMISSION_SEQ", sequenceName = "PERMISSION_SEQ", allocationSize = 1)
    private Long id;

    /**
     * The web domain a publisher blanket permission applies to. eg. www.act.com.au
     */
    @Column(name = "DOMAIN", length = 4000)
    private String domain;

    /**
     * The contact person who granted or denied this permission
     */
    @ManyToOne
    @JoinColumn(name = "INDIVIDUAL_ID")
    private ContactPerson contactPerson;

    @Column(name = "IS_BLANKET")
    private Boolean isBlanket;

    /**
     * The local reference number for files or record pertaining to this permission (within the NLA, this will be a
     * trim file number)
     */
    @Column(name = "LOCAL_REFERENCE", length = 16)
    private String localReference;

    /**
     * Any notes or extra conditions for this permission
     */
    @Column(name = "NOTE", length = 4000)
    private String note;

    @Column(name = "PERMISSION_DESCRIPTION", length = 256)
    private String description;

    @Column(name = "PERMISSION_STATE", length = 64)
    private String stateName;

    @Column(name = "PERMISSION_TYPE", length = 50)
    private String typeName;

    /**
     * The publisher who has the authority to grant this permission, if this is a blanket permission.
     */
    @ManyToOne
    @JoinColumn(name = "PUBLISHER_ID")
    private Publisher publisher;

    /**
     * The date on which this permission's status was determined.
     */
    @Column(name = "STATUS_SET_DATE")
    private Instant statusSetDate;

    /**
     * The title this permission refers to (if it is a title level permission).
     */
    @ManyToOne
    @JoinColumn(name = "TITLE_ID")
    private Title title;

    /**
     * State of this permission. eg Granted, Denied.
     */
    @ManyToOne
    @JoinColumn(name = "PERMISSION_STATE_ID")
    private PermissionState state;

    /**
     * The type of permission, ie. publisher (blanket) level or title level.
     */
    @ManyToOne
    @JoinColumn(name = "PERMISSION_TYPE_ID")
    private PermissionType type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Boolean getBlanket() {
        return isBlanket;
    }

    public void setBlanket(Boolean blanket) {
        isBlanket = blanket;
    }

    public String getLocalReference() {
        return localReference;
    }

    public void setLocalReference(String localReference) {
        this.localReference = localReference;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Instant getStatusSetDate() {
        return statusSetDate;
    }

    public void setStatusSetDate(Instant statusSetDate) {
        this.statusSetDate = statusSetDate;
    }

    public PermissionState getState() {
        return state;
    }

    public void setState(PermissionState state) {
        this.state = state;
    }

    public PermissionType getType() {
        return type;
    }

    public void setType(PermissionType type) {
        this.type = type;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public Individual getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(ContactPerson contactPerson) {
        this.contactPerson = contactPerson;
    }

    private boolean hasState(String stateName) {
        return stateName.equals(getStateName());
    }

    public boolean isDenied() {
        return hasState(PermissionState.DENIED);
    }

    public boolean isUnknown() {
        return hasState(PermissionState.UNKNOWN);
    }
}
