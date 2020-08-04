package pandas.admin.agency;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "AGENCY_AREA")
public class AgencyArea implements Serializable {
    @Id
    @Column(name = "AGENCY_AREA_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "AGENCY_ID", referencedColumnName = "AGENCY_ID")
    @JsonIgnore
    private Agency agency;

    @Column(name = "AREA_NAME")
    private String name;

    @Column(name = "AREA_WORDING")
    private String wording;

    @OneToMany(mappedBy = "area")
    private Collection<AgencyAreaIp> ips;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String areaName) {
        this.name = areaName;
    }

    public String getWording() {
        return this.wording;
    }

    public void setWording(String areaWording) {
        this.wording = areaWording;
    }

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public Collection<AgencyAreaIp> getIps() {
        return ips;
    }

    public void setIps(Collection<AgencyAreaIp> ips) {
        this.ips = ips;
    }
}
