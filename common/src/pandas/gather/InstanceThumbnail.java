package pandas.gather;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@IdClass(InstanceThumbnailId.class)
public class InstanceThumbnail {
    @Id
    @Column(name="INSTANCE_ID")
    private Long instanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="INSTANCE_ID")
    @MapsId
    @JsonIgnore
    private Instance instance;

    @Id
    @NotNull
    @ColumnDefault("0")
    private Type type = Type.REPLAY;

    private String contentType;

    private int status;

    @Lob
    @org.hibernate.annotations.Type(type = "org.hibernate.type.BinaryType")
    private byte[] data;

    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant lastModifiedDate;

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
        setInstanceId(instance.getId());
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long id) {
        this.instanceId = id;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        REPLAY,
        LIVE
    }
}
