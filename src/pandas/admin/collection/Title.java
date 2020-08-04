package pandas.admin.collection;

import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.search.annotations.*;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import pandas.admin.agency.Agency;
import pandas.admin.core.Individual;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Indexed
@NamedEntityGraph(name = "Title.subjects",
        attributeNodes = @NamedAttributeNode("subjects"))
public class Title {
    @Id
    @Column(name = "TITLE_ID")
    @JsonView(DataTablesOutput.View.class)
    private Long id;

    @Field
    @JsonView(DataTablesOutput.View.class)
    private Long pi;

    @Field
    @JsonView(DataTablesOutput.View.class)
    private String name;

    @Field
    private String titleUrl;

    @Field(analyze = Analyze.NO)
    @SortableField
    private LocalDateTime regDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEP_ID")
    private Tep tep;

    @ManyToOne
    @JoinColumn(name = "AGENCY_ID", referencedColumnName = "AGENCY_ID")
    private Agency agency;

    @ManyToOne
    @JoinColumn(name = "PUBLISHER_ID", referencedColumnName = "PUBLISHER_ID")
    private Publisher publisher;

    @ManyToMany(mappedBy = "titles")
    @IndexedEmbedded(includePaths = "name", includeEmbeddedObjectId = true)
    @OrderBy("name")
    private List<Subject> subjects;

    @ManyToOne
    @JoinColumn(name = "CURRENT_OWNER_ID")
    private Individual owner;

    @ManyToOne
    @JoinColumn(name = "CURRENT_STATUS_ID")
    private Status status;

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPi() {
        return pi;
    }

    public void setPi(Long pi) {
        this.pi = pi;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public LocalDateTime getRegDate() {
        return regDate;
    }

    public void setRegDate(LocalDateTime regDate) {
        this.regDate = regDate;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public Tep getTep() {
        return tep;
    }

    public String getTitleUrl() {
        return titleUrl;
    }

    public void setTitleUrl(String titleUrl) {
        this.titleUrl = titleUrl;
    }

    public boolean isVisible() {
        return getTep() != null && getTep().isDoCollection();
    }

    public Individual getOwner() {
        return owner;
    }

    public void setOwner(Individual owner) {
        this.owner = owner;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
