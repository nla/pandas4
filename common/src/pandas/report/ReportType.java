package pandas.report;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "REPORT_TYPE")
public class ReportType {
    private Long id;
    private String name;
    private String javaClass;
    private Boolean hasDetails;
    private Boolean hasPeriod;
    private Boolean hasAgency;
    private Boolean hasPublisherType;
    private Boolean hasRestrictionType;

    @Id
    @Column(name = "REPORT_TYPE_ID", nullable = false, precision = 0)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "NAME", nullable = false, length = 512)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "JAVA_CLASS", nullable = false, length = 256)
    public String getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
    }

    @Basic
    @Column(name = "HAS_DETAILS", nullable = false, precision = 0)
    public Boolean getHasDetails() {
        return hasDetails;
    }

    public void setHasDetails(Boolean hasDetails) {
        this.hasDetails = hasDetails;
    }

    @Basic
    @Column(name = "HAS_PERIOD", nullable = false, precision = 0)
    public Boolean getHasPeriod() {
        return hasPeriod;
    }

    public void setHasPeriod(Boolean hasPeriod) {
        this.hasPeriod = hasPeriod;
    }

    @Basic
    @Column(name = "HAS_AGENCY", nullable = false, precision = 0)
    public Boolean getHasAgency() {
        return hasAgency;
    }

    public void setHasAgency(Boolean hasAgency) {
        this.hasAgency = hasAgency;
    }

    @Basic
    @Column(name = "HAS_PUBLISHER_TYPE", nullable = false, precision = 0)
    public Boolean getHasPublisherType() {
        return hasPublisherType;
    }

    public void setHasPublisherType(Boolean hasPublisherType) {
        this.hasPublisherType = hasPublisherType;
    }

    @Basic
    @Column(name = "HAS_RESTRICTION_TYPE", nullable = false, precision = 0)
    public Boolean getHasRestrictionType() {
        return hasRestrictionType;
    }

    public void setHasRestrictionType(Boolean hasRestrictionType) {
        this.hasRestrictionType = hasRestrictionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportType that = (ReportType) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(javaClass, that.javaClass) && Objects.equals(hasDetails, that.hasDetails) && Objects.equals(hasPeriod, that.hasPeriod) && Objects.equals(hasAgency, that.hasAgency) && Objects.equals(hasPublisherType, that.hasPublisherType) && Objects.equals(hasRestrictionType, that.hasRestrictionType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, javaClass, hasDetails, hasPeriod, hasAgency, hasPublisherType, hasRestrictionType);
    }
}
