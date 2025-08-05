package pandas.social;

import jakarta.persistence.*;
import pandas.core.UseIdentityGeneratorIfMySQL;

@Entity
public class AttachmentArchiverState {
    @Id
    @UseIdentityGeneratorIfMySQL
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ATTACHMENT_ARCHIVER_STATE_SEQ")
    @SequenceGenerator(name = "ATTACHMENT_ARCHIVER_STATE_SEQ", sequenceName = "ATTACHMENT_ARCHIVER_STATE_SEQ", allocationSize = 1)
    private Long id;

    private String resumptionToken;

    private Long warcId;

    private Long warcOffset;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResumptionToken() {
        return resumptionToken;
    }

    public void setResumptionToken(String resumptionToken) {
        this.resumptionToken = resumptionToken;
    }

    public Long getWarcId() {
        return warcId;
    }

    public void setWarcId(Long warcId) {
        this.warcId = warcId;
    }

    public Long getWarcOffset() {
        return warcOffset;
    }

    public void setWarcOffset(Long warcOffset) {
        this.warcOffset = warcOffset;
    }
}
