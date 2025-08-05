package pandas.discovery;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pandas.agency.Agency;
import pandas.agency.User;
import pandas.collection.Reason;
import pandas.core.UseIdentityGeneratorIfMySQL;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
        @Index(name = "rejected_domain_agency_domain_idx", columnList = "agency_id, domain", unique = true),
})
public class RejectedDomain {
    @Id
    @Column(name = "ID", nullable = false)
    @UseIdentityGeneratorIfMySQL
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "REJECTED_DOMAIN_SEQ")
    @SequenceGenerator(name = "REJECTED_DOMAIN_SEQ", sequenceName = "REJECTED_DOMAIN_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private String domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REASON_ID", nullable = false)
    @NotNull
    private Reason reason;

    @CreatedDate
    private Instant createdDate;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    /**
     * Agency for agency-specific rejections. Null for system-wide rejections.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @Nullable
    @JoinColumn(name = "AGENCY_ID")
    private Agency agency;


    public RejectedDomain() {
    }

    public RejectedDomain(String domain, Reason reason, Agency agency) {
        this.domain = domain;
        this.reason = reason;
        this.agency = agency;
    }

    public String getDomain() {
        return domain;
    }

    public Reason getReason() {
        return reason;
    }

    public Agency getAgency() {
        return agency;
    }

    @Override
    public String toString() {
        return "RejectedDomain{" +
               "domain='" + domain + '\'' +
               ", reason='" + reason + '\'' +
               (agency == null ? "" : ", agency=" + agency.getOrganisation().getAlias()) +
               '}';
    }

    public Long getId() {
        return id;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public User getUser() {
        return user;
    }
}
