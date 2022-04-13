package pandas.gather;

import java.time.Instant;

public record InstanceEvent(long id, Instant date, long instanceId, long titleId, String titleName) {
}
