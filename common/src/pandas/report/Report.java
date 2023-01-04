package pandas.report;

import pandas.agency.Agency;
import pandas.agency.User;
import pandas.collection.PublisherType;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Report {
    @Id
    @Column(name = "REPORT_ID", nullable = false)
    private Long id;

    /**
     * Individual who requested this report.
     */
    @ManyToOne
    @JoinColumn(name = "INDIVIDUAL_ID", nullable = false)
    private User owner;

    /**
     * Agency that this report covers.
     */
    @ManyToOne
    @JoinColumn(name = "AGENCY_ID", nullable = true)
    private Agency agency;

    /**
     * Start date for the period this report covers. Should be null for scheduled reports.
     */
    @Column(name = "PERIOD_START", nullable = true)
    private Instant periodStart;

    /**
     * End date for the period this report covers. Should be null for scheduled reports.
     */
    @Column(name = "PERIOD_END", nullable = true)
    private Instant periodEnd;

    /**
     * Detail level to be included in the report. 0 = "Numbers only" summary. 1 = full details.
     */
    @Column(name = "SHOW_DETAILS", nullable = false)
    private Boolean showDetails;

    /**
     * Publisher type this report should cover. Null if not applicable for this report type.
     */
    @ManyToOne
    @JoinColumn(name = "PUBLISHER_TYPE_ID", nullable = true)
    private PublisherType publisherTypeId;

    /**
     * Restriction type this report should cover (if applicable). null=N/A, 0=period, 1=date, 2=auth
     */
    @Column(name = "RESTRICTION_TYPE", nullable = true)
    private Long restrictionType;

    /**
     * Date this report was last generated.
     */
    @Column(name = "LAST_GENERATION_DATE", nullable = true)
    private Instant lastGenerationDate;

    /**
     * Date this report should next be generated. Null if the report is not scheduled (ie ad-hoc report).
     */
    @Column(name = "NEXT_GENERATION_DATE", nullable = true)
    private Instant nextGenerationDate;

    /**
     * Day of the week or month (depending on schedule_id) that this report should be delivered upon.
     */
    @Column(name = "SCHEDULED_DAY", nullable = true)
    private Long scheduledDay;

    /**
     * Should this report appear in the user's report tray? 0=hidden, 1=visible
     */
    @Column(name = "IS_VISIBLE", nullable = false)
    private Boolean isVisible;

    /**
     * Error message if the report generation failed
     */
    @Column(name = "ERROR_MSG", nullable = true, length = 512)
    private String errorMsg;

    /**
     * The type of report that was selected.
     */
    @ManyToOne
    @JoinColumn(name = "REPORT_TYPE_ID", referencedColumnName = "REPORT_TYPE_ID", nullable = false)
    private ReportType type;

    /**
     * the schedule this report is on. Null if the report is not scheduled.
     */
    @ManyToOne
    @JoinColumn(name = "REPORT_SCHEDULE_ID", referencedColumnName = "REPORT_SCHEDULE_ID")
    private ReportSchedule schedule;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getShowDetails() {
        return showDetails;
    }

    public void setShowDetails(Boolean showDetails) {
        this.showDetails = showDetails;
    }

    public Long getRestrictionType() {
        return restrictionType;
    }

    public void setRestrictionType(Long restrictionType) {
        this.restrictionType = restrictionType;
    }

    public Long getScheduledDay() {
        return scheduledDay;
    }

    public void setScheduledDay(Long scheduledDay) {
        this.scheduledDay = scheduledDay;
    }

    public Boolean getVisible() {
        return isVisible;
    }

    public void setVisible(Boolean visible) {
        isVisible = visible;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    public ReportSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(ReportSchedule schedule) {
        this.schedule = schedule;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public Instant getLastGenerationDate() {
        return lastGenerationDate;
    }

    public void setLastGenerationDate(Instant lastGenerationDate) {
        this.lastGenerationDate = lastGenerationDate;
    }

    public Instant getNextGenerationDate() {
        return nextGenerationDate;
    }

    public void setNextGenerationDate(Instant nextGenerationDate) {
        this.nextGenerationDate = nextGenerationDate;
    }
}
