package pandas.collection;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Information about, or to be displayed on, a Title Entry Page (TEP)
 */
@Entity
public class Tep {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "TEP_SEQ")
    @SequenceGenerator(name = "TEP_SEQ", sequenceName = "TEP_SEQ", allocationSize = 1)
    @Column(name = "TEP_ID", nullable = false, precision = 0)
    private Long id;

    /**
     * Copyright and/or disclaimer for the publisher wishes to appear for this title.
     */
    @Column(name = "COPYRIGHT_NOTE", nullable = true, length = 4000)
    private String copyrightNote;

    /**
     * URL to a copyright statement or disclaimer for this title.
     */
    @Column(name = "COPYRIGHT_URL", nullable = true, length = 1024)
    private String copyrightUrl;

    /**
     * Time at which the TEP was created.
     */
    @Column(name = "DISPLAY_DATE", nullable = true)
    private Instant displayDate;

    /**
     * The title/heading to be displayed on the TEP.
     */
    @Column(name = "DISPLAY_TITLE", nullable = true, length = 4000)
    private String displayTitle;

    /**
     * Should the display system list this title in any collections it belongs to.
     */
    @Column(name = "DO_COLLECTION", nullable = true)
    private Boolean doCollection;

    /**
     * Should the display system list this title in search results.
     */
    @Column(name = "DO_SEARCH", nullable = true)
    private Boolean doSearch;

    /**
     * Should the display system should list this title under any subjects it belongs to.
     */
    @Column(name = "DO_SUBJECT", nullable = true)
    private Boolean doSubject;

    @Column(name = "GENERAL_NOTE", nullable = true, length = 4000)
    private String generalNote;

    /**
     * Indicates whether the statement for this TEP is a copyright statement. (can also be a disclaimer)
     */
    @Column(name = "HAS_COPYRIGHT", nullable = true, precision = 0)
    private Boolean hasCopyright;

    /**
     * Indicates whether the statement for this TEP is a disclaimer (can also be a copyright statement)
     */
    @Column(name = "HAS_DISCLAIMER", nullable = true, precision = 0)
    private Boolean hasDisclaimer;

    /**
     * The number of times this TEP has been shown in the display system. (Not currently updated.)
     */
    @Column(name = "HIT_COUNT", nullable = true, precision = 0)
    private Long hitCount;

    /**
     * Whether the TEP is to appear in the display system at all.
     */
    @Column(name = "IS_PUBLISHED", nullable = true, precision = 0)
    private Boolean isPublished;

    /**
     * Metadata to be placed in the header of the TEP. (No longer used.)
     */
    @Column(name = "METADATA", nullable = true)
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String metadata;

    /**
     * Should new instances of this title be publisher on TEP automatically upon archiving.
     */
    @Column(name = "PUBLISH_IMMEDIATELY", nullable = false)
    private boolean publishImmediately;

    /**
     * The title this TEP corresponds to.
     */
    @OneToOne
    @JoinColumn(name = "TITLE_ID")
    private Title title;

    @ManyToOne
    @JoinColumn(name = "COPYRIGHT_TYPE_ID")
    private CopyrightType copyrightType;

    @OneToMany(mappedBy = "tep", orphanRemoval = true, cascade = CascadeType.ALL)
    @OrderBy("order, id desc")
    private List<IssueGroup> issueGroups = new ArrayList<>();

    public Tep() {
        doCollection = true;
        doSubject = true;
        doSearch = true;
        displayDate = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCopyrightNote() {
        return copyrightNote;
    }

    public void setCopyrightNote(String copyrightNote) {
        this.copyrightNote = copyrightNote;
    }

    public String getCopyrightUrl() {
        return copyrightUrl;
    }

    public void setCopyrightUrl(String copyrightUrl) {
        this.copyrightUrl = copyrightUrl;
    }

    public Instant getDisplayDate() {
        return displayDate;
    }

    public void setDisplayDate(Instant displayDate) {
        this.displayDate = displayDate;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public Boolean getDoCollection() {
        return doCollection;
    }

    public void setDoCollection(Boolean doCollection) {
        this.doCollection = doCollection;
    }

    public Boolean getDoSearch() {
        return doSearch;
    }

    public void setDoSearch(Boolean doSearch) {
        this.doSearch = doSearch;
    }

    public Boolean getDoSubject() {
        return doSubject;
    }

    public void setDoSubject(Boolean doSubject) {
        this.doSubject = doSubject;
    }

    public String getGeneralNote() {
        return generalNote;
    }

    public void setGeneralNote(String generalNote) {
        this.generalNote = generalNote;
    }

    public Boolean getHasCopyright() {
        return hasCopyright;
    }

    public void setHasCopyright(Boolean hasCopyright) {
        this.hasCopyright = hasCopyright;
    }

    public Boolean getHasDisclaimer() {
        return hasDisclaimer;
    }

    public void setHasDisclaimer(Boolean hasDisclaimer) {
        this.hasDisclaimer = hasDisclaimer;
    }

    public Long getHitCount() {
        return hitCount;
    }

    public void setHitCount(Long hitCount) {
        this.hitCount = hitCount;
    }

    public Boolean getPublished() {
        return isPublished;
    }

    public void setPublished(Boolean published) {
        isPublished = published;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public boolean isPublishImmediately() {
        return publishImmediately;
    }

    public void setPublishImmediately(boolean publishImmediately) {
        this.publishImmediately = publishImmediately;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public CopyrightType getCopyrightType() {
        return copyrightType;
    }

    public void setCopyrightType(CopyrightType copyrightType) {
        this.copyrightType = copyrightType;
    }

    public List<IssueGroup> getIssueGroups() {
        return Collections.unmodifiableList(issueGroups);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tep tep = (Tep) o;
        return Objects.equals(id, tep.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void removeAllIssueGroups() {
        issueGroups.clear();
    }

    public void addIssueGroup(IssueGroup group) {
        issueGroups.add(group);
        group.setTep(this);
    }
}
