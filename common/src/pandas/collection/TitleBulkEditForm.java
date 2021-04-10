package pandas.collection;

import pandas.core.Individual;
import pandas.gather.GatherMethod;
import pandas.gather.GatherSchedule;

import java.util.List;

public class TitleBulkEditForm {
    private List<Title> titles;

    private boolean editMethod;
    private GatherMethod method;

    private boolean editSchedule;
    private GatherSchedule schedule;

    private boolean editOwner;
    private Individual owner;

    private boolean editAnbdNumber;
    private String anbdNumber;

    private boolean editAddNote;
    private String addNote;

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

    public Individual getOwner() {
        return owner;
    }

    public void setOwner(Individual owner) {
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
}
