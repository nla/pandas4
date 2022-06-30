package pandas.gather;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;
import pandas.collection.Status;
import pandas.collection.Title;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;

/**
 * Information about the gather settings and options for a title.
 */
@Entity
@Table(name = "TITLE_GATHER")
@DynamicUpdate
public class TitleGather {
    @Id
    @Column(name="TITLE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ACTIVE_PROFILE_ID")
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Profile activeProfile;

    @Column(name = "ADDITIONAL_URLS", length = 4000)
    private String additionalUrls;

    @Column(name = "AUTHENTICATE_IP")
    private Long authenticateIp;

    @Column(name = "AUTHENTICATE_USER")
    private Long authenticateUser;

    @Column(name = "CAL_START_DATE")
    private Instant calStartDate;

    @Column(name = "FIRST_GATHER_DATE")
    @GenericField(sortable = Sortable.YES)
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

    @Column(name = "GATHER_URL", length = 1024)
    private String gatherUrl;

    @Column(name = "LAST_GATHER_DATE")
    @GenericField(sortable = Sortable.YES)
    private Instant lastGatherDate;

    @Column(name = "NEXT_GATHER_DATE")
    @GenericField(sortable = Sortable.YES)
    private Instant nextGatherDate;

    @Column(name = "NOTES", length = 4000)
    @FullTextField(analyzer = "english")
    private String notes;

    @Column(name = "PASSWORD", length = 128)
    private String password;

    @Column(name = "QUEUED")
    private Long queued;

    @Column(name = "IS_SCHEDULED")
    private Long isScheduled;

    /**
     * The next recurring date for titles with a recurring schedule set. Note: This does not include one-off gather
     * dates (see nextGatherDate for the actual next gather date including one-offs).
     */
    @Column(name = "SCHEDULED_DATE")
    private Instant scheduledDate;

    @Column(name = "USERNAME", length = 128)
    private String username;

    @Column(name = "GATHER_COMMAND")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String gatherCommand;

    @OneToOne
    @JoinColumn(name="TITLE_ID")
    @MapsId
    @JsonIgnore
    private Title title;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "gather")
    private List<GatherDate> oneoffDates = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "T_GATHER_ARG",
            joinColumns = @JoinColumn(name = "TITLE_GATHER_ID"),
            inverseJoinColumns = @JoinColumn(name = "OPTION_ARGUMENT_ID"),
            indexes = { @Index(name = "t_gather_arg_title_gather_id_index", columnList = "TITLE_GATHER_ID"),
                    @Index(name = "t_gather_arg_option_argument_id_index", columnList = "OPTION_ARGUMENT_ID") })
    private List<OptionArgument> optionArguments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "SCOPE_ID")
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    private Scope scope;

    public TitleGather() {
    }

    public String getAdditionalUrls() {
        return this.additionalUrls;
    }

    public List<String> getAdditionalUrlList() {
        if (getAdditionalUrls() == null || getAdditionalUrls().isBlank()) return Collections.emptyList();
        return Arrays.asList(getAdditionalUrls().split("\\s+"));
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

    public List<GatherDate> getOneoffDates() {
        return oneoffDates;
    }

    public void replaceOneoffDates(List<Instant> instants) {
        var oldInstants = oneoffDates.stream().map(GatherDate::getDate).collect(toSet());
        var newInstants = new HashSet<>(instants);

        // remove any dates no longer present
        oneoffDates.removeIf(d -> !newInstants.contains(d.getDate()));

        // add any novel dates
        for (var instant : newInstants) {
            if (!oldInstants.contains(instant)) {
                oneoffDates.add(new GatherDate(this, instant));
            }
        }
    }

    public void addOneoffDate(Instant instant) {
        oneoffDates.add(new GatherDate(this, instant));
    }

    public void calculateNextGatherDate() {
        if (title.getStatus().getId().equals(Status.CEASED_ID)) {
            setNextGatherDate(null);
            return;
        }

        Instant next = Instant.MAX;

        // first check the earliest one-off date
        if (!getOneoffDates().isEmpty()) {
            Instant date = Collections.min(getOneoffDates(), comparing(GatherDate::getDate)).getDate();
            if (date.isBefore(next)) next = date;
        }

        // next try the title-level scheduled date
        if (getSchedule() != null && !getSchedule().isNone()) {
            Instant scheduledDate = getScheduledDate();
            // if we don't have a scheduled date or its out of date then recalculate it
            if (scheduledDate == null || (getLastGatherDate() != null && !scheduledDate.isAfter(getLastGatherDate()))) {
                scheduledDate = getSchedule().calculateNextTime(getLastGatherDate());
                setScheduledDate(scheduledDate);
            }
            if (scheduledDate != null && scheduledDate.isBefore(next)) next = scheduledDate;
        }

        // now see if any collections have schedules
        for (var collection: getTitle().getCollections()) {
            if (collection.getGatherSchedule() == null) continue;
            Instant date = collection.getGatherSchedule().calculateNextTime(getLastGatherDate());
            if (date != null && date.isBefore(next)) next = date;
        }

        if (next == Instant.MAX) next = null;
        setNextGatherDate(next);
    }

    public void addOptionArgument(OptionArgument argument) {
        optionArguments.add(argument);
    }

    public List<OptionArgument> getOptionArguments() {
        return Collections.unmodifiableList(optionArguments);
    }

    public void setOptionArguments(List<OptionArgument> arguments) {
        this.optionArguments = arguments;
    }

    public OptionArgument getFiltersOptionArgument() {
        for (var argument : getOptionArguments()) {
            if (argument.getOption().isGatherFilters()) {
                return argument;
            }
        }
        return null;
    }

    public String getFilters() {
        var argument = getFiltersOptionArgument();
        return argument == null ? null : argument.getArgument();
    }


    /**
     * Constructs the HTTrack gather command line.
     */
    public String buildHttrackCommand() {
        StringBuilder sb = new StringBuilder();
        for (OptionArgument argument : getOptionArguments()) {
            argument.toCommandLine(sb);
        }

        if (getScope() != null && getScope().getDepth() != null) {
            sb.append("--depth=").append(getScope().getDepth() + 1).append(" ");
        }
        Profile profile = getActiveProfile();
        if (profile != null) {
            if (profile.getCrawlLimitBytes() != null) {
                sb.append("--max-size=").append(profile.getCrawlLimitBytes()).append(" ");
            }
            if (profile.getCrawlLimitSeconds() != null) {
                sb.append("--max-time=").append(profile.getCrawlLimitSeconds()).append(" ");
            }
        }

        String url = getGatherUrl();
        if (getAuthenticateUser() != null && getAuthenticateUser() == 1 && getUsername() != null && getPassword() != null) {
            if (url.startsWith("http://")) {
                url = "http://" + getUsername() + ":" + getPassword() + "@" + url.substring("http://".length());
            } else if (url.startsWith("https://")) {
                url = "https://" + getUsername() + ":" + getPassword() + "@" + url.substring("https://".length());
            }
        }

        if (url == null) url = getTitle().getSeedUrl();
        if (url == null) url = getTitle().getTitleUrl();

        sb.append("'").append(url.replace("'", "'\"'\"'")).append("'");

        for (String extra: getAdditionalUrlList()) {
            sb.append(" '").append(extra.replace("'", "'\"'\"'")).append("'");
        }

        return sb.toString();
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
