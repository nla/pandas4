package pandas.discovery;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import org.netpreserve.urlcanon.ParsedUrl;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pandas.agency.User;
import pandas.collection.Title;
import pandas.core.View;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Indexed
public class Discovery {
    @Id
    @Column(name = "DISCOVERY_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "DISCOVERY_SEQ")
    @SequenceGenerator(name = "DISCOVERY_SEQ", sequenceName = "DISCOVERY_SEQ", allocationSize = 1)
    @JsonView(View.Summary.class)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "DISCOVERY_SOURCE_ID")
    @IndexedEmbedded(includePaths = {"id"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    @JsonView(View.Summary.class)
    private DiscoverySource source;

    @Column(length = 1024)
    @FullTextField(analyzer = "url")
    @JsonView(View.Summary.class)
    private String sourceUrl;

    @NotBlank
    @Column(length = 1024)
    @FullTextField(analyzer = "url")
    @JsonView(View.Summary.class)
    private String url;

    @Column(length = 1024)
    @FullTextField(analyzer = "english")
    @JsonView(View.Summary.class)
    private String name;

    @Column(length = 1024)
    @FullTextField(analyzer = "english")
    @JsonView(View.Summary.class)
    private String description;

    private String state;

    private String postcode;

    private String locality;

    @CreatedDate
    @GenericField(sortable = Sortable.YES)
    @JsonView(View.Summary.class)
    private Instant createdDate;

    @CreatedBy
    @ManyToOne
    @JoinColumn(name = "CREATED_BY")
    @JsonIgnore
    private User createdBy;

    @LastModifiedDate
    @JsonView(View.Summary.class)
    private Instant lastModifiedDate;

    @LastModifiedBy
    @ManyToOne
    @JoinColumn(name = "LAST_MODIFIED_BY")
    @JsonIgnore
    private User lastModifiedBy;

    @ManyToOne
    @JoinColumn(name = "TITLE_ID")
    private Title title;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DiscoverySource getSource() {
        return source;
    }

    public void setSource(DiscoverySource source) {
        this.source = source;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @GenericField
    @IndexingDependency(derivedFrom = {@ObjectPath(@PropertyValue(propertyName = "url"))})
    public boolean isDotAu() {
        return ParsedUrl.parseUrl(url).getHost().endsWith(".au");
    }

    @GenericField
    @IndexingDependency(derivedFrom = {@ObjectPath(@PropertyValue(propertyName = "title"))})
    public boolean isAlreadySelected() {
        return title != null;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }
}
