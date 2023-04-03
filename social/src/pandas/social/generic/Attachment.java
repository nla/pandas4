package pandas.social.generic;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

public record Attachment(
        @NotNull URI url,
        @NotNull String type,
        String altText
) {
}
