package pandas.gather;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pandas.agency.User;
import pandas.collection.Title;

import java.sql.Types;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Entity
@Indexed
@Table(name = "INSTANCE",
        indexes = @Index(name = "instance_title_id_instance_date_index", columnList = "title_id, instance_date"))
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
public class Instance {
    public static final DateTimeFormatter instanceDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")
            .withZone(ZoneId.systemDefault());

    @Id
    @Column(name = "INSTANCE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INSTANCE_SEQ")
    @SequenceGenerator(name = "INSTANCE_SEQ", sequenceName = "INSTANCE_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "TITLE_ID")
    @IndexedEmbedded(includePaths = {
            "agency.id",
            "collectionAncestry.id",
            "collections.id",
            "gather.method.id",
            "gather.schedule.id",
            "id",
            "owner.id",
            "subjects.id",
    })
    private Title title;

    @Column(name = "INSTANCE_DATE")
    @GenericField(sortable = Sortable.YES)
    private Instant date;

    @Column(name = "CURRENT_STATE_ID", nullable = false)
    @GenericField(aggregable = Aggregable.YES)
    private State state;

    @Column(name = "DISPLAY_NOTE", length = 4000)
    private String displayNote;

    @Column(name = "GATHER_METHOD_NAME")
    private String gatherMethodName;

    @Column(name = "GATHERED_URL", length = 1024)
    private String gatheredUrl;

    @Column(name = "INSTANCE_STATE_ID")
    @Deprecated(forRemoval = true)
    private Long instanceStateId;

    @Column(name = "INSTANCE_STATUS_ID")
    @Deprecated(forRemoval = true)
    private Long instanceStatusId;

    @Column(name = "IS_DISPLAYED")
    private Boolean isDisplayed;

    @Column(name = "PREFIX")
    private String prefix;

    @Column(name = "PROCESSABLE")
    @Deprecated(forRemoval = true)
    private Long processable;

    @Column(name = "REMOVEABLE")
    @Deprecated(forRemoval = true)
    private Long removeable;

    @Column(name = "RESOURCE_ID")
    @Deprecated(forRemoval = true)
    private Long resourceId;

    @Column(name = "RESTRICTABLE")
    @Deprecated(forRemoval = true)
    private Long restrictable;

    @Column(name = "RESTRICTION_ENABLED_T")
    @Deprecated(forRemoval = true)
    private Long restrictionEnabledT;

    @Column(name = "TEP_URL")
    @Lob
    @JdbcTypeCode(Types.LONGVARCHAR)
    private String tepUrl;

    @Column(name = "TRANSPORTABLE")
    @Deprecated(forRemoval = true)
    private Long transportable;

    @Column(name = "TYPE_NAME")
    private String typeName;

    @Column(name = "GATHER_COMMAND")
    @Lob
    @JdbcTypeCode(Types.LONGVARCHAR)
    private String gatherCommand;

    @OneToOne(mappedBy = "instance")
    // XXX: not sure why it can't find the inverse automatically
    @AssociationInverseSide(
            inversePath = @ObjectPath( @PropertyValue( propertyName = "instance" ) )
    )
    private InstanceGather gather;

    @OneToMany(mappedBy = "instance")
    private List<PandasExceptionLog> exceptions = new ArrayList<>();

    @OneToMany(mappedBy = "instance")
    private List<InstanceThumbnail> thumbnails;

    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<StateHistory> stateHistory = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="SCOPE_ID")
    private Scope scope;

    @ManyToOne
    @JoinColumn(name="PROFILE_ID")
    private Profile profile;

    @GenericField(sortable = Sortable.YES, projectable = Projectable.YES)
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    private Instant lastModifiedDate;

    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstanceSeed> seeds = new ArrayList<>();

    protected Instance() {
    }

    @NullMarked
    public Instance(Title title, Instant date, String gatherMethod) {
        this.title = Objects.requireNonNull(title);
        this.date = Objects.requireNonNull(date);
        this.prefix = "PAN";
        if (title.getGather() != null) {
            String gatherUrl = title.getGather().getGatherUrl();
            if (gatherUrl != null) {
                setTepUrl("/pan/" + title.getPi() + "/" + getDateString() + "/" + gatherUrl.replaceFirst("^https?://", ""));
                this.gatheredUrl = gatherUrl;
            }
            this.scope = title.getGather().getScope();
            this.profile = title.getGather().getActiveProfile();
        }
        this.gatherMethodName = Objects.requireNonNull(gatherMethod);
        this.processable = 1L; // unused?
        this.removeable = 1L; // unused?
        this.restrictable = 1L; // unused?
        this.transportable = 1L; // unused?
        changeState(State.CREATION, null, date);
    }

    public State getState() {
        return this.state;
    }

    @NullMarked
    public void changeState(State newState, @Nullable User user, Instant changeDate) {
        if (newState.equals(state)) return;

        // Mark the end of the previous state history record, if one exists
        if (!this.stateHistory.isEmpty()) {
            this.stateHistory.get(this.stateHistory.size() - 1).setEndDate(changeDate);
        }

        this.stateHistory.add(new StateHistory(this, newState, changeDate, user));
        this.state = newState;
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

    public String getGatheredUrl() {
        return this.gatheredUrl;
    }

    public Instant getDate() {
        return this.date;
    }

    public String getDateString() {
        return getDateZoned().format(instanceDateFormat);
    }

    private ZonedDateTime getDateZoned() {
        return getDate().atZone(ZoneId.systemDefault());
    }

    public Long getId() {
        return this.id;
    }

    public String getHumanId() {
        return "nla.arc-" + getTitle().getPi() + "-" + getDateString();
    }

    public String getBrowsertrixCollectionName() {
        return getHumanId().replace('.', '-');
    }

    public Boolean getIsDisplayed() {
        return this.isDisplayed;
    }

    public void setIsDisplayed(Boolean isDisplayed) {
        this.isDisplayed = isDisplayed;
    }

    public boolean shouldDisplayOnTep() {
        return isDisplayed != null && isDisplayed && getState().isArchived();
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getTepUrl() {
        return this.tepUrl;
    }

    private static final Pattern NPH_REGEX = Pattern.compile("/nph-arch(/\\d{4})?/[a-zA-Z]\\d{4}-[a-zA-Z]{3}-\\d{1,2}/(.*)$");
    private static final Pattern OLYMPICS_SPECIAL_CASE = Pattern.compile("^/parchive/2000/(?:olympics|paralympic.org)/");
    private static final Pattern PARCHIVE_REGEX = Pattern.compile("^/parchive/(.*)$");

    public String getTepUrlAbsolute() {
        if (GatherMethod.HTTRACK.equals(getGatherMethodName()) ||
                GatherMethod.UPLOAD.equals(getGatherMethodName()) ||
                getGatherMethodName() == null) {
            return tepUrlToAbsolute(getTepUrl());
        } else if (getTepUrl().startsWith("/pan/")) {
            return getGatheredUrl();
        } else {
            return getTepUrl();
        }
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

    public String getTypeName() {
        return this.typeName;
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

    public InstanceGather getGather() {
        if (gather == null) {
            return new InstanceGather();
        }
        return gather;
    }

    /**
     * Shorthand for instance.getTitle().getPi() because we use this a lot.
     */
    public Long getPi() {
        return getTitle() == null ? null : getTitle().getPi();
    }

    public boolean canArchive() {
        return getState().isGathered();
    }

    public boolean canPostProcess() {
        return isUploadMethod() && getState().isCreation();
    }

    public boolean canFindAndReplace() {
        return getState().isGathered() &&
                Set.of(GatherMethod.HTTRACK, GatherMethod.UPLOAD).contains(getGatherMethodName());
    }

    public boolean canDelete() {
        return switch (getState()) {
            case GATHERING, GATHERED, CREATION, FAILED, CHECKING, CHECKED -> true;
            default -> false;
        };
    }

    public boolean canStop() {
        return getState() == State.GATHERING;
    }

    public boolean canEdit() {
        return getState().isArchivedOrArchiving() || getState().isGathered();
    }

    public boolean isWorkingAreaAccessible() {
        return getState().isGathered() || (isUploadMethod() && getState().isCreation());
    }

    public List<PandasExceptionLog> getExceptions() {
        return exceptions;
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
        Instant date = Instant.from(instanceDateFormat.withZone(ZoneId.systemDefault()).parse(dateString));
        Title title = new Title();
        title.setGather(new TitleGather());
        title.getGather().setGatherUrl("http://test");
        title.setPi(pi);
        return new Instance(title, date, GatherMethod.DEFAULT);
    }

    public InstanceThumbnail getLiveThumbnail() {
        for (var thumbnail : thumbnails) {
            if (thumbnail.getType() == InstanceThumbnail.Type.LIVE) {
                return thumbnail;
            }
        }
        return null;
    }

    public List<StateHistory> getStateHistory() {
        return Collections.unmodifiableList(stateHistory);
    }

    public Instant getLastStateChangeDate() {
        if (getStateHistory().isEmpty()) return date;
        return stateHistory.get(stateHistory.size() - 1).getStartDate();
    }

    public State getStateBeforeFailure() {
        if (!getState().isFailed()) return null;
        for (int i = getStateHistory().size() - 1; i >= 0; i--) {
            var state = getStateHistory().get(i).getState();
            if (!state.isFailed()) {
                return state;
            }
        }
        return null;
    }

    public Scope getScope() {
        return scope;
    }

    public Profile getProfile() {
        return profile;
    }

    public Set<GatherProblem> getProblemsInternal() {
        var problems = new HashSet<GatherProblem>();
        if (getGather().hasSizeWarning()) problems.add(GatherProblem.SIZE_WARNING);
        if (problems.isEmpty()) problems.add(GatherProblem.NO_PROBLEMS);
        if (getSeeds().stream().anyMatch(InstanceSeed::isError)) problems.add(GatherProblem.SEED_ERROR);
        return Collections.unmodifiableSet(problems);
    }

    public Set<String> getProblems() {
        return getProblemsInternal().stream().map(GatherProblem::text).collect(Collectors.toSet());
    }

    @GenericField(aggregable = Aggregable.YES)
    @IndexingDependency(derivedFrom = {
            @ObjectPath({@PropertyValue(propertyName = "gather"), @PropertyValue(propertyName = "size")}),
    })
    public Set<Long> getProblemIds() {
        return getProblemsInternal().stream().map(p -> (long)p.ordinal()).collect(Collectors.toSet());
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public boolean isHeritrixMethod() {
        return GatherMethod.HERITRIX.equals(getGatherMethodName());
    }

    public boolean isBrowsertrixMethod() {
        return GatherMethod.BROWSERTRIX.equals(getGatherMethodName());
    }

    public boolean isUploadMethod() {
        return GatherMethod.UPLOAD.equals(getGatherMethodName());
    }

    public void setSeeds(List<InstanceSeed> seeds) {
        this.seeds.clear();
        this.seeds.addAll(seeds);
        for (var seed : seeds) {
            seed.setInstance(this);
        }
    }

    public List<InstanceSeed> getSeeds() {
        return Collections.unmodifiableList(seeds);
    }

    public InstanceSeed getSeed(String url) {
        return getSeeds().stream().filter(seed -> seed.getUrl().equals(url)).findFirst().orElse(null);
    }

    @NullMarked
    public void retryAfterFailure(@Nullable User user, Instant changeDate) {
        if (!getState().isFailed()) return;
        State stateBeforeFailure = getStateBeforeFailure();
        if (!stateBeforeFailure.canBeRetried()) return;
        changeState(stateBeforeFailure, user, changeDate);
    }

    @NullMarked
    public void delete(@Nullable User user, Instant changeDate) {
        if (getState().isDeletedOrDeleting()) return;
        if (!canDelete()) {
            throw new IllegalStateException("can't delete instance in state " + getState());
        }
        changeState(State.DELETING, user, changeDate);
    }
}
