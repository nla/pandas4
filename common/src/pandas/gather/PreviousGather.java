package pandas.gather;

import java.time.Instant;

public class PreviousGather {
    private final long currentInstanceId;
    private final long id;
    private final Instant date;
    private final Long files;
    private final Long size;

    public PreviousGather(long currentInstanceId, long id, Instant date, Long files, Long size) {
        this.currentInstanceId = currentInstanceId;
        this.id = id;
        this.date = date;
        this.files = files;
        this.size = size;
    }

    public long getCurrentInstanceId() {
        return currentInstanceId;
    }

    public long getId() {
        return id;
    }

    public Long getSize() {
        return size;
    }

    public Long getFiles() {
        return files;
    }

    public Instant getDate() {
        return date;
    }

    public String getStats() {
        return InstanceGather.formatStats(getFiles(), getSize());
    }
}
