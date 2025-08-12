package pandas.collection;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    @GenericField
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
    @Deprecated
    private PermissionState state;

    /**
     * The type of permission, ie. publisher (blanket) level or title level.
     */
    @ManyToOne
    @JoinColumn(name = "PERMISSION_TYPE_ID")
    @Deprecated
    private PermissionType type;

    @OneToMany(mappedBy = "permission")
    private List<Title> titles = new ArrayList<>();

    public Permission() {
    }

    public Permission(Publisher publisher) {
        setPublisher(publisher);
        setBlanket(true);
        setStateName(PermissionState.UNKNOWN);
    }

    public Permission(Title title) {
        setTitle(title);
        setBlanket(false);
        setStateName(PermissionState.UNKNOWN);
    }

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
        description = blanket ? "Pandas3 Publisher Blanket Permission" : "Pandas4 Default Title Permission";
        typeName = blanket ? PermissionType.PUBLISHER : PermissionType.TITLE;
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

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        if (!PermissionState.ALL_NAMES.contains(stateName)) {
            throw new IllegalArgumentException("Invalid permission state: " + stateName);
        }
        this.stateName = stateName;
    }

    public String getTypeName() {
        return typeName;
    }

    public Instant getStatusSetDate() {
        return statusSetDate;
    }

    public LocalDate getStatusSetLocalDate() {
        if (statusSetDate == null) return null;
        return statusSetDate.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void setStatusSetDate(Instant statusSetDate) {
        this.statusSetDate = statusSetDate;
    }

    public void setStatusSetLocalDate(LocalDate localDate) {
        this.statusSetDate = localDate == null ? null : localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    /**
     * @deprecated Use {@link #getStateName()} instead
     */
    @Deprecated
    public PermissionState getState() {
        return state;
    }

    /**
     * @deprecated Use {@link #getTypeName()} instead
     */
    @Deprecated
    public PermissionType getType() {
        return type;
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

    public ContactPerson getContactPerson() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public boolean isBlanket() {
        return getBlanket() != null && getBlanket();
    }

    public long getTitleCount() {
        return Hibernate.size(titles);
    }

    public boolean isImpossible() {
        return hasState(PermissionState.IMPOSSIBLE);
    }
}
