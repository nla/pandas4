package pandas.gather;

import pandas.collection.Title;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "INSTANCE")
public class Instance {
    @Id
    @Column(name = "INSTANCE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "TITLE_ID")
    private Title title;

    @Column(name = "INSTANCE_DATE")
    private Instant date;

    @ManyToOne
    @JoinColumn(name = "CURRENT_STATE_ID")
    private State state;

    @Column(name = "DISPLAY_NOTE")
    private String displayNote;

    @Column(name = "GATHER_METHOD_NAME")
    private String gatherMethodName;

    @Column(name = "GATHERED_URL")
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
    private String tepUrl;

    @Column(name = "TITLE_ID")
    private Long titleId;

    @Column(name = "TRANSPORTABLE")
    private Long transportable;

    @Column(name = "TYPE_NAME")
    private String typeName;

    @Column(name = "GATHER_COMMAND")
    private String gatherCommand;


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

    public void setDate(Instant instanceDate) {
        this.date = instanceDate;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
