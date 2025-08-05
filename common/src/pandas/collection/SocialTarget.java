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
import pandas.core.UseIdentityGeneratorIfMySQL;
import pandas.core.View;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class SocialTarget {
    @Id
    @Column(name = "SOCIAL_TARGET_ID")
    @UseIdentityGeneratorIfMySQL
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

    public Long getOldestPostIdLong() {
        return oldestPostId == null ? null : Long.parseUnsignedLong(oldestPostId);
    }

    public String getNewestPostId() {
        return newestPostId;
    }

    public Long getNewestPostIdLong() {
        return newestPostId == null ? null : Long.parseUnsignedLong(newestPostId);
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

    public void setCurrentRangePositionLong(Long currentRangePosition) {
        this.currentRangePosition = currentRangePosition == null ? null : currentRangePosition.toString();
    }

    public String getCurrentRangeEnd() {
        return currentRangeEnd;
    }

    public void setCurrentRangeEnd(String currentRangeEnd) {
        this.currentRangeEnd = currentRangeEnd;
    }

    public void setCurrentRangeEndLong(Long currentRangeEnd) {
        this.currentRangeEnd = currentRangeEnd == null ? null : currentRangeEnd.toString();
    }

    public void setLastVisitedDate(Instant lastVisitedDate) {
        this.lastVisitedDate = lastVisitedDate;
    }

    public void incrementPostCount(long delta) {
        postCount += delta;
    }

    public void setNewestPost(Long id, Instant date) {
        newestPostId = id == null ? null : id.toString();
        newestPostDate = date;
    }

    public void setOldestPost(Long id, Instant date) {
        oldestPostId = id == null ? null : id.toString();
        oldestPostDate = date;
    }

    public Long getCurrentRangePositionLong() {
        return currentRangePosition == null ? null : Long.parseUnsignedLong(currentRangePosition);
    }

    public Long getCurrentRangeEndLong() {
        return currentRangeEnd == null ? null : Long.parseUnsignedLong(currentRangeEnd);
    }

    public void expandOldestAndNewest(long postId, Instant createdAt) {
        expandNewest(postId, createdAt);
        expandOldest(postId, createdAt);
    }

    public void expandNewest(long postId, Instant createdAt) {
        if (newestPostId == null || Long.compareUnsigned(postId, getNewestPostIdLong()) > 0) {
            setNewestPost(postId, createdAt);
        }
    }

    public void expandOldest(long postId, Instant createdAt) {
        if (oldestPostId == null || Long.compareUnsigned(postId, getOldestPostIdLong()) < 0) {
            setOldestPost(postId, createdAt);
        }
    }

    public void clearRange() {
        setCurrentRangePosition(null);
        setCurrentRangeEnd(null);
    }
}
