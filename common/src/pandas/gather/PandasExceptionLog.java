package pandas.gather;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.*;

import java.sql.Types;
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
    @JdbcTypeCode(Types.LONGVARCHAR)
    private String detail;

    public PandasExceptionLog(Instant date, Instance instance, String summary, String detail, String originator, Long pi, long viewed) {
        this.date = date;
        this.instance = instance;
        this.summary = summary;
        this.detail = detail;
        this.originator = originator;
        this.pi = pi;
        this.viewed = viewed;
    }

    public Instant getDate() {
        return this.date;
    }

    public Instance getInstance() {
        return instance;
    }

    public String getOriginator() {
        return this.originator;
    }

    public Long getId() {
        return this.id;
    }

    public Long getPi() {
        return this.pi;
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

    public Long getViewed() {
        return this.viewed;
    }

    public String getDetail() {
        return this.detail;
    }
}
