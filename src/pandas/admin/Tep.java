package pandas.admin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Tep {
    @Id
    @Column(name = "TEP_ID")
    private Long id;

    @OneToOne(mappedBy = "tep", optional = false)
    private Title title;

    private String copyrightNote;
    private String copyrightUrl;
    private String displayTitle;
    private Boolean doCollection;
    private Boolean doSearch;
    private Boolean doSubject;
    private String generalNote;
    private Boolean hasCopyright;
    private Boolean hasDisclaimer;
    private Long hitCount;
    private Boolean isPublished;
    private String metadata;

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public boolean isDoCollection() {
        return doCollection != null && doCollection;
    }

    public boolean isDoSearch() {
        return doSearch;
    }

    public boolean isDoSubject() {
        return doSubject;
    }

    public boolean isHasCopyright() {
        return hasCopyright;
    }

    public boolean isHasDisclaimer() {
        return hasDisclaimer;
    }
}
