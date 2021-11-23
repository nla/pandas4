package pandas.discovery;

import com.fasterxml.jackson.annotation.JsonView;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pandas.agency.User;
import pandas.core.View;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class DiscoverySource {
    @Id
    @Column(name = "DISCOVERY_SOURCE_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "DISCOVERY_SEQ")
    @SequenceGenerator(name = "DISCOVERY_SEQ", sequenceName = "DISCOVERY_SEQ", allocationSize = 1)
    @GenericField
    @JsonView(View.Summary.class)
    private Long id;

    @NotBlank
    @JsonView(View.Summary.class)
    private String name;

    @JsonView(View.Summary.class)
    private String url;

    // spider options
    private String itemQuery;
    private String itemNameQuery;
    private String itemLinkQuery;
    private String itemDescriptionQuery;
    private String linkQuery;

    @CreatedDate
    private Instant createdDate;

    @CreatedBy
    @ManyToOne
    @JoinColumn(name = "CREATED_BY")
    @JsonIgnore
    private User createdBy;

    @LastModifiedDate
    private Instant lastModifiedDate;

    @LastModifiedBy
    @ManyToOne
    @JoinColumn(name = "LAST_MODIFIED_BY")
    @JsonIgnore
    private User lastModifiedBy;

    public DiscoverySourceForm toForm() {
        return new DiscoverySourceForm(name, url, itemQuery, itemNameQuery, itemLinkQuery, itemDescriptionQuery, linkQuery);
    }

    public void update(DiscoverySourceForm form) {
        name = form.name();
        url = form.url();
        itemQuery = form.itemQuery();
        itemNameQuery = form.itemNameQuery();
        itemLinkQuery = form.itemLinkQuery();
        itemDescriptionQuery = form.itemDescriptionQuery();
        linkQuery = form.linkQuery();
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getItemQuery() {
        return itemQuery;
    }

    public void setItemQuery(String itemQuery) {
        this.itemQuery = itemQuery;
    }

    public String getItemNameQuery() {
        return itemNameQuery;
    }

    public void setItemNameQuery(String itemNameQuery) {
        this.itemNameQuery = itemNameQuery;
    }

    public String getItemLinkQuery() {
        return itemLinkQuery;
    }

    public void setItemLinkQuery(String itemLinkQuery) {
        this.itemLinkQuery = itemLinkQuery;
    }

    public String getItemDescriptionQuery() {
        return itemDescriptionQuery;
    }

    public void setItemDescriptionQuery(String itemDescriptionQuery) {
        this.itemDescriptionQuery = itemDescriptionQuery;
    }

    public String getLinkQuery() {
        return linkQuery;
    }

    public void setLinkQuery(String linkQuery) {
        this.linkQuery = linkQuery;
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
}
