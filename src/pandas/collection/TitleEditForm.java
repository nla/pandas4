package pandas.collection;

import pandas.gather.GatherSchedule;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class TitleEditForm {
    private Long id;
    @NotBlank
    private String titleUrl;
    @NotBlank
    private String name;
    @NotNull
    private GatherSchedule gatherSchedule;
    @NotNull
    private Format format;
    private String anbdNumber;
    private String localReference;
    private String localDatabaseNo;
    private List<Collection> collections;
    private List<Subject> subjects;
    @NotNull
    private Status status;

    public TitleEditForm() {}

    public TitleEditForm(Title title) {
        id = title.getId();
        titleUrl = title.getTitleUrl();
        name = title.getName();
        gatherSchedule = title.getGather() == null ? null : title.getGather().getSchedule();
        format = title.getFormat();
        anbdNumber = title.getAnbdNumber();
        localReference = title.getLocalReference();
        localDatabaseNo = title.getLocalDatabaseNo();
        collections = title.getCollections();
        subjects = title.getSubjects();
        status = title.getStatus();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitleUrl() {
        return titleUrl;
    }

    public void setTitleUrl(String titleUrl) {
        this.titleUrl = titleUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GatherSchedule getGatherSchedule() {
        return gatherSchedule;
    }

    public void setGatherSchedule(GatherSchedule gatherSchedule) {
        this.gatherSchedule = gatherSchedule;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public String getAnbdNumber() {
        return anbdNumber;
    }

    public void setAnbdNumber(String anbdNumber) {
        this.anbdNumber = anbdNumber;
    }

    public String getLocalReference() {
        return localReference;
    }

    public void setLocalReference(String localReference) {
        this.localReference = localReference;
    }

    public String getLocalDatabaseNo() {
        return localDatabaseNo;
    }

    public void setLocalDatabaseNo(String localDatabaseNo) {
        this.localDatabaseNo = localDatabaseNo;
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
