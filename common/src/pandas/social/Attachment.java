package pandas.social;

import java.net.URI;

public record Attachment(
        URI url,
        String type,
        String altText
) {
}
