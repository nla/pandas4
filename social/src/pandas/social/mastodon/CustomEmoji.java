package pandas.social.mastodon;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

// https://docs.joinmastodon.org/entities/CustomEmoji/
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CustomEmoji(
        String shortcode,
        String url,
        String staticUrl,
        boolean visibleInPicker,
        String category
) {
}
