package pandas.core;

import java.time.Instant;

public interface IThumbnail {
    byte[] getData();

    Instant getLastModifiedDate();

    String getContentType();
}
