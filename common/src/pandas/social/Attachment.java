package pandas.social;

import java.util.List;

public record Attachment(
        String url,
        String type,
        String altText,
        List<Source> sources
) {
    public record Source(String url, String contentType, Long bitrate, Integer width, Integer height) {
    }
}
