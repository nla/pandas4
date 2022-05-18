package pandas.collection;

import pandas.agency.User;
import pandas.gather.GatherMethod;
import pandas.gather.GatherSchedule;
import pandas.gather.Scope;

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

    private boolean editScope;
    private Scope scope;

    private boolean editOwner;
    private User owner;

    private boolean editAnbdNumber;
    private String anbdNumber;

    private boolean editAddNote;
    private String addNote;

    private List<Collection> collectionsToAdd = new ArrayList<>();

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TitleBulkEditForm[");
        if (isEditMethod()) sb.append(" method=").append(method.getId());
        if (isEditSchedule()) sb.append(" schedule=").append(method.getId());
        if (isEditOwner()) sb.append(" owner=").append(owner.getId());
        if (isEditAnbdNumber()) sb.append(" anbdNumber=").append(anbdNumber);
        if (isEditAddNote()) sb.append(" addNote=").append(addNote);
        sb.append(" titles=[");
        for (Title title: titles) {
            sb.append(title.getId());
            sb.append(",");
        }
        sb.append("]");
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
        this.collectionsToAdd = collectionsToAdd;
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
}
