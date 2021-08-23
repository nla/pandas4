package pandas.gather;

import java.io.Serializable;
import java.util.Objects;

public class InstanceThumbnailId implements Serializable {
    private Long instanceId;
    private InstanceThumbnail.Type type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceThumbnailId that = (InstanceThumbnailId) o;
        return Objects.equals(instanceId, that.instanceId) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceId, type);
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public InstanceThumbnail.Type getType() {
        return type;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public void setType(InstanceThumbnail.Type type) {
        this.type = type;
    }
}
