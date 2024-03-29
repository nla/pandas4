package pandas.collection;

import org.springframework.format.annotation.DateTimeFormat;
import pandas.agency.User;
import pandas.gather.GatherMethod;
import pandas.gather.GatherSchedule;
import pandas.gather.Profile;
import pandas.gather.Scope;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TitleBulkEditForm {
    private List<Title> titles;

    private boolean editStatus;
    private Status status;
    private Reason reason;

    private boolean editMethod;
    private GatherMethod method;

    private boolean editSchedule;
    private GatherSchedule schedule;

    private boolean editOneoffDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate oneoffDate;

    private boolean editScope;
    private Scope scope;

    private boolean editOwner;
    private User owner;

    private boolean editAnbdNumber;
    private String anbdNumber;

    private boolean editAddNote;
    private String addNote;

    private final List<Collection> collectionsToAdd = new ArrayList<>();
    private final List<Collection> collectionsToRemove = new ArrayList<>();

    private final List<Subject> subjectsToAdd = new ArrayList<>();
    private final List<Subject> subjectsToRemove = new ArrayList<>();

    private boolean editProfile;
    private Profile profile;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TitleBulkEditForm[");
        if (isEditMethod()) sb.append(" method=").append(method.getId());
        if (isEditSchedule()) sb.append(" schedule=").append(schedule.getId());
        if (isEditOwner()) sb.append(" owner=").append(owner.getId());
        if (isEditAnbdNumber()) sb.append(" anbdNumber=").append(anbdNumber);
        if (isEditAddNote()) sb.append(" addNote=").append(addNote);
        sb.append(" titles=[");
        for (Title title : titles) {
            sb.append(title.getId());
            sb.append(",");
        }
        sb.append("]");
        if (!subjectsToAdd.isEmpty()) {
            sb.append(" subjectsToAdd=[");
            for (Subject subject : subjectsToAdd) {
                sb.append(subject.getId());
                sb.append(",");
            }
            sb.append("]");
        }
        if (!collectionsToAdd.isEmpty()) {
            sb.append(" collectionsToAdd=[");
            for (Collection collection : collectionsToAdd) {
                sb.append(collection.getId());
                sb.append(",");
            }
            sb.append("]");
        }
        return sb.toString();
    }

    public List<Title> getTitles() {
        return titles;
    }

    public void setTitles(List<Title> titles) {
        this.titles = titles;
    }

    public boolean isEditMethod() {
        return editMethod;
    }

    public void setEditMethod(boolean editMethod) {
        this.editMethod = editMethod;
    }

    public GatherMethod getMethod() {
        return method;
    }

    public void setMethod(GatherMethod method) {
        this.method = method;
    }

    public boolean isEditSchedule() {
        return editSchedule;
    }

    public void setEditSchedule(boolean editSchedule) {
        this.editSchedule = editSchedule;
    }

    public GatherSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(GatherSchedule schedule) {
        this.schedule = schedule;
    }

    public boolean isEditOwner() {
        return editOwner;
    }

    public void setEditOwner(boolean editOwner) {
        this.editOwner = editOwner;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public boolean isEditAnbdNumber() {
        return editAnbdNumber;
    }

    public void setEditAnbdNumber(boolean editAnbdNumber) {
        this.editAnbdNumber = editAnbdNumber;
    }

    public String getAnbdNumber() {
        return anbdNumber;
    }

    public void setAnbdNumber(String anbdNumber) {
        this.anbdNumber = anbdNumber;
    }

    public boolean isEditAddNote() {
        return editAddNote;
    }

    public void setEditAddNote(boolean editAddNote) {
        this.editAddNote = editAddNote;
    }

    public String getAddNote() {
        return addNote;
    }

    public void setAddNote(String addNote) {
        this.addNote = addNote;
    }

    public List<Collection> getCollectionsToAdd() {
        return collectionsToAdd;
    }

    public void setCollectionsToAdd(List<Collection> collectionsToAdd) {
        this.collectionsToAdd.clear();
        this.collectionsToAdd.addAll(collectionsToAdd);
    }

    public List<Collection> getCollectionsToRemove() {
        return collectionsToRemove;
    }

    public void setCollectionsToRemove(List<Collection> collectionsToRemove) {
        this.collectionsToRemove.clear();
        this.collectionsToRemove.addAll(collectionsToRemove);
    }

    public List<Subject> getSubjectsToAdd() {
        return subjectsToAdd;
    }

    public void setSubjectsToAdd(List<Subject> subjectsToAdd) {
        this.subjectsToAdd.clear();
        this.subjectsToAdd.addAll(subjectsToAdd);
    }

    public List<Subject> getSubjectsToRemove() {
        return subjectsToRemove;
    }

    public void setSubjectsToRemove(List<Subject> subjectsToRemove) {
        this.subjectsToRemove.clear();
        this.subjectsToRemove.addAll(subjectsToRemove);
    }

    public boolean isEditScope() {
        return editScope;
    }

    public void setEditScope(boolean editScope) {
        this.editScope = editScope;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public boolean isEditStatus() {
        return editStatus;
    }

    public void setEditStatus(boolean editStatus) {
        this.editStatus = editStatus;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public boolean isEditOneoffDate() {
        return editOneoffDate;
    }

    public void setEditOneoffDate(boolean editOneoffDate) {
        this.editOneoffDate = editOneoffDate;
    }

    public LocalDate getOneoffDate() {
        return oneoffDate;
    }

    public void setOneoffDate(LocalDate oneoffDate) {
        this.oneoffDate = oneoffDate;
    }

    public boolean isEditProfile() {
        return editProfile;
    }

    public void setEditProfile(boolean editProfile) {
        this.editProfile = editProfile;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
