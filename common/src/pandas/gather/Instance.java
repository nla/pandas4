package pandas.gather;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import pandas.collection.Title;

import javax.persistence.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Entity
@Table(name = "INSTANCE",
        indexes = @Index(name = "instance_title_id_instance_date_index", columnList = "title_id, instance_date"))
@DynamicUpdate
public class Instance {
    static final DateTimeFormatter instanceDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    @Id
    @Column(name = "INSTANCE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INSTANCE_SEQ")
    @SequenceGenerator(name = "INSTANCE_SEQ", sequenceName = "INSTANCE_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "TITLE_ID")
    private Title title;

    @Column(name = "INSTANCE_DATE")
    private Instant date;

    @ManyToOne
    @JoinColumn(name = "CURRENT_STATE_ID")
    private State state;

    @Column(name = "DISPLAY_NOTE", length = 4000)
    private String displayNote;

    @Column(name = "GATHER_METHOD_NAME")
    private String gatherMethodName;

    @Column(name = "GATHERED_URL", length = 1024)
    private String gatheredUrl;

    @Column(name = "INSTANCE_STATE_ID")
    private Long instanceStateId;

    @Column(name = "INSTANCE_STATUS_ID")
    private Long instanceStatusId;

    @Column(name = "IS_DISPLAYED")
    private Long isDisplayed;

    @Column(name = "PREFIX")
    private String prefix;

    @Column(name = "PROCESSABLE")
    private Long processable;

    @Column(name = "REMOVEABLE")
    private Long removeable;

    @Column(name = "RESOURCE_ID")
    private Long resourceId;

    @Column(name = "RESTRICTABLE")
    private Long restrictable;

    @Column(name = "RESTRICTION_ENABLED_T")
    private Long restrictionEnabledT;

    @Column(name = "TEP_URL")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String tepUrl;

    @Column(name = "TRANSPORTABLE")
    private Long transportable;

    @Column(name = "TYPE_NAME")
    private String typeName;

    @Column(name = "GATHER_COMMAND")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String gatherCommand;

    @OneToOne(mappedBy = "instance")
    private InstanceGather gather;

    @OneToMany(mappedBy = "instance")
    private List<PandasExceptionLog> exceptions;

    @OneToMany(mappedBy = "instance")
    private List<InstanceThumbnail> thumbnails;

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getDisplayNote() {
        return this.displayNote;
    }

    public void setDisplayNote(String displayNote) {
        this.displayNote = displayNote;
    }

    public String getGatherMethodName() {
        return this.gatherMethodName;
    }

    public void setGatherMethodName(String gatherMethodName) {
        this.gatherMethodName = gatherMethodName;
    }

    public String getGatheredUrl() {
        return this.gatheredUrl;
    }

    public void setGatheredUrl(String gatheredUrl) {
        this.gatheredUrl = gatheredUrl;
    }

    public Instant getDate() {
        return this.date;
    }

    public String getDateString() {
        return getDateZoned().format(instanceDateFormat);
    }

    public void setDateString(String dateString) {
        setDate(Instant.from(instanceDateFormat.withZone(ZoneId.systemDefault()).parse(dateString)));
    }

    private ZonedDateTime getDateZoned() {
        return getDate().atZone(ZoneId.systemDefault());
    }

    public void setDate(Instant instanceDate) {
        this.date = instanceDate;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHumanId() {
        return "nla.arc-" + getTitle().getPi() + "-" + getDateString();
    }

    public Long getInstanceStateId() {
        return this.instanceStateId;
    }

    public void setInstanceStateId(Long instanceStateId) {
        this.instanceStateId = instanceStateId;
    }

    public Long getInstanceStatusId() {
        return this.instanceStatusId;
    }

    public void setInstanceStatusId(Long instanceStatusId) {
        this.instanceStatusId = instanceStatusId;
    }

    public Long getIsDisplayed() {
        return this.isDisplayed;
    }

    public void setIsDisplayed(Long isDisplayed) {
        this.isDisplayed = isDisplayed;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Long getProcessable() {
        return this.processable;
    }

    public void setProcessable(Long processable) {
        this.processable = processable;
    }

    public Long getRemoveable() {
        return this.removeable;
    }

    public void setRemoveable(Long removeable) {
        this.removeable = removeable;
    }

    public Long getResourceId() {
        return this.resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getRestrictable() {
        return this.restrictable;
    }

    public void setRestrictable(Long restrictable) {
        this.restrictable = restrictable;
    }

    public Long getRestrictionEnabledT() {
        return this.restrictionEnabledT;
    }

    public void setRestrictionEnabledT(Long restrictionEnabledT) {
        this.restrictionEnabledT = restrictionEnabledT;
    }

    public String getTepUrl() {
        return this.tepUrl;
    }


    private static final Pattern NPH_REGEX = Pattern.compile("/nph-arch(/\\d{4})?/[a-zA-Z]\\d{4}-[a-zA-Z]{3}-\\d{1,2}/(.*)$");
    private static final Pattern OLYMPICS_SPECIAL_CASE = Pattern.compile("^/parchive/2000/(?:olympics|paralympic.org)/");
    private static final Pattern PARCHIVE_REGEX = Pattern.compile("^/parchive/(.*)$");

    public String getTepUrlAbsolute() {
        return tepUrlToAbsolute(getTepUrl());
    }

    public static String tepUrlToAbsolute(String url) {
        if (url.startsWith("/pan/")) {
            return "http://pandora.nla.gov.au" + url;
        }
        if (url.startsWith("/parchive/")) {
            url = OLYMPICS_SPECIAL_CASE.matcher(url).replaceFirst("/parchive/");
            url = PARCHIVE_REGEX.matcher(url).replaceFirst("/nph-arch/$1");
        }
        if (url.startsWith("/nph-arch/")) {
            url = NPH_REGEX.matcher(url).replaceFirst("$2");
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            return url;
        } else {
            return url;
        }
    }

    public void setTepUrl(String tepUrl) {
        this.tepUrl = tepUrl;
    }

    public Long getTransportable() {
        return this.transportable;
    }

    public void setTransportable(Long transportable) {
        this.transportable = transportable;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getGatherCommand() {
        return this.gatherCommand;
    }

    public void setGatherCommand(String gatherCommand) {
        this.gatherCommand = gatherCommand;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public InstanceGather getGather() {
        return gather;
    }

    /**
     * Shorthand for instance.getTitle().getPi() because we use this a lot.
     */
    public Long getPi() {
        return getTitle() == null ? null : getTitle().getPi();
    }

    public boolean canArchive() {
        return State.GATHERED.equals(getState().getName());
    }

    public boolean canDelete() {
        return Set.of(State.GATHERING, State.GATHERED, State.CREATION).contains(getState().getName());
    }

    public boolean canStop() {
        return getState().getName().equals(State.GATHERING);
    }

    public List<PandasExceptionLog> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<PandasExceptionLog> exceptions) {
        this.exceptions = exceptions;
    }

    public InstanceThumbnail getThumbnail() {
        for (var thumbnail : thumbnails) {
            if (thumbnail.getType() == InstanceThumbnail.Type.REPLAY) {
                return thumbnail;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instance instance = (Instance) o;
        return Objects.equals(id, instance.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns true if this instance used a gather method like HTTrack or Upload which consists of flat files in the
     * working area (instead of a container format like WARC).
     */
    public boolean isFlatFiles() {
        return "HTTrack".equals(getGatherMethodName()) || "Upload".equals(getGatherMethodName());
    }

    public static Instance createDummy(long pi, String dateString) {
        Instance instance = new Instance();
        instance.setDateString(dateString);
        Title title = new Title();
        title.setPi((long)pi);
        instance.setTitle(title);
        return instance;
    }

    public InstanceThumbnail getLiveThumbnail() {
        for (var thumbnail : thumbnails) {
            if (thumbnail.getType() == InstanceThumbnail.Type.LIVE) {
                return thumbnail;
            }
        }
        return null;
    }
}
