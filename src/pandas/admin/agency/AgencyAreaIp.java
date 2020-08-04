package pandas.admin.agency;

import javax.persistence.*;

@Entity
@Table(name = "AGENCY_AREA_IP")
public class AgencyAreaIp {
    @Id
    @Column(name = "AGENCY_AREA_IP_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "AGENCY_AREA_ID")
    private AgencyArea area;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "MASK")
    private String mask;

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long agencyAreaIpId) {
        this.id = agencyAreaIpId;
    }

    public String getMask() {
        return this.mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }
}
