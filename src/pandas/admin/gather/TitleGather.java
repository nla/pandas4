package pandas.admin.gather;

import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;
import pandas.admin.collection.Title;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "TITLE_GATHER")
public class TitleGather {
    @Id
    @Column(name="TITLE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ACTIVE_PROFILE_ID")
    private Profile activeProfile;

    @Column(name = "ADDITIONAL_URLS")
    private String additionalUrls;

    @Column(name = "AUTHENTICATE_IP")
    private Long authenticateIp;

    @Column(name = "AUTHENTICATE_USER")
    private Long authenticateUser;

    @Column(name = "CAL_START_DATE")
    private Instant calStartDate;

    @Column(name = "FIRST_GATHER_DATE")
    private Instant firstGatherDate;

    @ManyToOne
    @JoinColumn(name = "GATHER_METHOD_ID")
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private GatherMethod method;

    @ManyToOne
    @JoinColumn(name = "GATHER_SCHEDULE_ID")
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private GatherSchedule schedule;

    @Column(name = "GATHER_URL")
    private String gatherUrl;

    @Column(name = "LAST_GATHER_DATE")
    private Instant lastGatherDate;

    @Column(name = "NEXT_GATHER_DATE")
    @GenericField
    private Instant nextGatherDate;

    @Column(name = "NOTES")
    @FullTextField(analyzer = "english")
    private String notes;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "QUEUED")
    private Long queued;

    @Column(name = "IS_SCHEDULED")
    private Long isScheduled;

    @Column(name = "SCHEDULED_DATE")
    private Instant scheduledDate;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "GATHER_COMMAND")
    private String gatherCommand;

    @OneToOne(mappedBy = "gather", optional = false)
    private Title title;

    public TitleGather() {
    }

    public String getAdditionalUrls() {
        return this.additionalUrls;
    }

    public void setAdditionalUrls(String additionalUrls) {
        this.additionalUrls = additionalUrls;
    }

    public Long getAuthenticateIp() {
        return this.authenticateIp;
    }

    public void setAuthenticateIp(Long authenticateIp) {
        this.authenticateIp = authenticateIp;
    }

    public Long getAuthenticateUser() {
        return this.authenticateUser;
    }

    public void setAuthenticateUser(Long authenticateUser) {
        this.authenticateUser = authenticateUser;
    }

    public Instant getCalStartDate() {
        return this.calStartDate;
    }

    public void setCalStartDate(Instant calStartDate) {
        this.calStartDate = calStartDate;
    }

    public Instant getFirstGatherDate() {
        return this.firstGatherDate;
    }

    public void setFirstGatherDate(Instant firstGatherDate) {
        this.firstGatherDate = firstGatherDate;
    }

    public GatherMethod getMethod() {
        return this.method;
    }

    public void setMethod(GatherMethod gatherMethod) {
        this.method = gatherMethod;
    }

    public GatherSchedule getSchedule() {
        return this.schedule;
    }

    public void setSchedule(GatherSchedule gatherSchedule) {
        this.schedule = gatherSchedule;
    }

    public String getGatherUrl() {
        return this.gatherUrl;
    }

    public void setGatherUrl(String gatherUrl) {
        this.gatherUrl = gatherUrl;
    }

    public Instant getLastGatherDate() {
        return this.lastGatherDate;
    }

    public void setLastGatherDate(Instant lastGatherDate) {
        this.lastGatherDate = lastGatherDate;
    }

    public Instant getNextGatherDate() {
        return this.nextGatherDate;
    }

    public void setNextGatherDate(Instant nextGatherDate) {
        this.nextGatherDate = nextGatherDate;
    }

    public String getNotes() {
        return this.notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getQueued() {
        return this.queued;
    }

    public void setQueued(Long queued) {
        this.queued = queued;
    }

    public Long getIsScheduled() {
        return this.isScheduled;
    }

    public void setIsScheduled(Long isScheduled) {
        this.isScheduled = isScheduled;
    }

    public Instant getScheduledDate() {
        return this.scheduledDate;
    }

    public void setScheduledDate(Instant scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGatherCommand() {
        return this.gatherCommand;
    }

    public void setGatherCommand(String gatherCommand) {
        this.gatherCommand = gatherCommand;
    }

    public Profile getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(Profile activeProfile) {
        this.activeProfile = activeProfile;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }
}
