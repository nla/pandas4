package pandas.gather;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "PANDAS_EXCEPTION_LOG")
public class PandasExceptionLog {
    @Id
    @Column(name = "EXCEPTION_LOG_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PANDAS_EXCEPTION_LOG_SEQ")
    @SequenceGenerator(name = "PANDAS_EXCEPTION_LOG_SEQ", sequenceName = "PANDAS_EXCEPTION_LOG_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "EXCEPTION_DATE")
    private Instant date;

    @ManyToOne
    @JoinColumn(name = "INSTANCE_ID")
    private Instance instance;

    @Column(name = "EXCEPTION_ORIGINATOR", length = 100)
    private String originator;

    @Column(name = "PI")
    private Long pi;

    @Column(name = "EXCEPTION_SUMMARY", length = 4000)
    private String summary;

    @Column(name = "TITLE_ID")
    private Long titleId;

    @Column(name = "EXCEPTION_VIEWED")
    private Long viewed;

    @Column(name = "EXCEPTION_DETAIL")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String detail;

    public Instant getDate() {
        return this.date;
    }

    public void setDate(Instant exceptionDate) {
        this.date = exceptionDate;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public String getOriginator() {
        return this.originator;
    }

    public void setOriginator(String exceptionOriginator) {
        this.originator = exceptionOriginator;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long exceptionLogId) {
        this.id = exceptionLogId;
    }

    public Long getPi() {
        return this.pi;
    }

    public void setPi(Long pi) {
        this.pi = pi;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String exceptionSummary) {
        this.summary = exceptionSummary;
    }

    public Long getTitleId() {
        return this.titleId;
    }

    public void setTitleId(Long titleId) {
        this.titleId = titleId;
    }

    public Long getViewed() {
        return this.viewed;
    }

    public void setViewed(Long exceptionViewed) {
        this.viewed = exceptionViewed;
    }

    public String getDetail() {
        return this.detail;
    }

    public void setDetail(String exceptionDetail) {
        this.detail = exceptionDetail;
    }
}
