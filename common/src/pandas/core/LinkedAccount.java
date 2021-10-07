package pandas.core;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;

/**
 * An account in an external system such as login server that can be used to authenticate this user.
 */
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "LINKED_ACCOUNT", indexes = {
        @Index(name = "LINKED_ACCOUNT_EXTERNAL_ID_INDEX", columnList = "provider, external_id"),
})
public class LinkedAccount {
    @Id
    @Column(name = "LINKED_ACCOUNT_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "LINKED_ACCOUNT_SEQ")
    @SequenceGenerator(name = "LINKED_ACCOUNT_SEQ", sequenceName = "LINKED_ACCOUNT_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    private Individual individual;

    /**
     * An identifier authentication provider. For OIDC this currently means the Spring Security client registration id.
     */
    @Column(name = "PROVIDER", nullable = false)
    private String provider;

    /**
     * The identifier the linked account provider uses for this account. For OAuth2/OIDC providers this will be the
     * 'sub' token claim.
     */
    @Column(name = "EXTERNAL_ID", nullable = false)
    private String externalId;

    @CreatedDate
    @Column(nullable = false)
    private Instant createdDate;

    @LastModifiedDate
    @Column
    private Instant lastModifiedDate;

    @Column
    private Instant lastLoginDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String linkedId) {
        this.externalId = linkedId;
    }

    public Instant getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Instant lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
}
