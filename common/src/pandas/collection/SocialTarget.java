package pandas.collection;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pandas.agency.User;
import pandas.core.View;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class SocialTarget {
    @Id
    @Column(name = "SOCIAL_TARGET_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SOCIAL_TARGET_SEQ")
    @SequenceGenerator(name = "SOCIAL_TARGET_SEQ", sequenceName = "SOCIAL_TARGET_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    @JsonView(View.Summary.class)
    private Long id;

    @NotNull
    private String server;

    @NotNull
    private String query;

    private long postCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TITLE_ID")
    private Title title;

    private String oldestPostId;
    private String newestPostId;

    private Instant oldestPostDate;
    private Instant newestPostDate;

    private Instant lastVisitedDate;

    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant lastModifiedDate;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_BY")
    private User createdBy;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LAST_MODIFIED_BY")
    private User lastModifiedBy;

    private String currentRangePosition;
    private String currentRangeEnd;

    public SocialTarget() {
    }

    public SocialTarget(String server, String query, Title title) {
        this.server = server;
        this.query = query;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getServer() {
        return server;
    }

    public String getQuery() {
        return query;
    }

    public long getPostCount() {
        return postCount;
    }

    public Title getTitle() {
        return title;
    }

    public String getOldestPostId() {
        return oldestPostId;
    }

    public String getNewestPostId() {
        return newestPostId;
    }

    public Instant getOldestPostDate() {
        return oldestPostDate;
    }

    public Instant getNewestPostDate() {
        return newestPostDate;
    }

    public Instant getLastVisitedDate() {
        return lastVisitedDate;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }


    public User getCreatedBy() {
        return createdBy;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    @Override
    public String toString() {
        return "SocialTarget{" +
                "id=" + id +
                ", server='" + server + '\'' +
                ", query='" + query + '\'' +
                '}';
    }

    public String getCurrentRangePosition() {
        return currentRangePosition;
    }

    public void setCurrentRangePosition(String currentRangePosition) {
        this.currentRangePosition = currentRangePosition;
    }

    public String getCurrentRangeEnd() {
        return currentRangeEnd;
    }

    public void setCurrentRangeEnd(String currentRangeEnd) {
        this.currentRangeEnd = currentRangeEnd;
    }

    public void setLastVisitedDate(Instant lastVisitedDate) {
        this.lastVisitedDate = lastVisitedDate;
    }

    public void incrementPostCount(long delta) {
        postCount += delta;
    }

    public void setNewestPost(String id, Instant date) {
        newestPostId = id;
        newestPostDate = date;
    }

    public void setOldestPost(String id, Instant date) {
        oldestPostId = id;
        oldestPostDate = date;
    }
}
