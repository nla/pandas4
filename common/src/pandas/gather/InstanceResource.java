package pandas.gather;

import javax.persistence.*;

@Entity
@Table(name = "INS_RESOURCE")
public class InstanceResource {
    @Column(name = "DISPLAY_URL")
    private String displayUrl;

    @Column(name = "GATHERED_URL")
    private String gatheredUrl;

    @Id
    @Column(name = "INSTANCE_ID")
    private Long instanceId;

    @OneToOne
    @JoinColumn(name="INSTANCE_ID")
    @MapsId
    private Instance instance;

    @Column(name = "LOCAL_URL")
    private String localUrl;

    public String getDisplayUrl() {
        return this.displayUrl;
    }

    public void setDisplayUrl(String displayUrl) {
        this.displayUrl = displayUrl;
    }

    public String getGatheredUrl() {
        return this.gatheredUrl;
    }

    public void setGatheredUrl(String gatheredUrl) {
        this.gatheredUrl = gatheredUrl;
    }

    public Long getInstanceId() {
        return this.instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getLocalUrl() {
        return this.localUrl;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}
