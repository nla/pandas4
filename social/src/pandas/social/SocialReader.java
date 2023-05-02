package pandas.social;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.netpreserve.jwarc.WarcReader;
import org.netpreserve.jwarc.WarcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.social.twitter.AdaptiveSearch;
import pandas.social.twitter.TimelineV2;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class SocialReader implements Closeable {
    public static final Pattern TWEETS_AND_REPLIES_RE = Pattern.compile("https://twitter.com/i/api/graphql/[^/]+/UserTweetsAndReplies\\?.*");
    private final Logger log = LoggerFactory.getLogger(SocialReader.class);
    private final WarcReader warcReader;
    private final String warcFilename;
    private WarcResponse warcResponse;

    public SocialReader(WarcReader warcReader) {
        this.warcReader = warcReader;
        this.warcFilename = null;
    }

    public SocialReader(Path path) throws IOException {
        this(new WarcReader(path));
    }

    public SocialReader(WarcReader warcReader, String warcFilename) {
        this.warcReader = warcReader;
        this.warcFilename = warcFilename;
    }

    public List<Post> nextBatch() throws IOException {
        while (true) {
            var warcRecord = warcReader.next().orElse(null);
            if (warcRecord == null) return null;
            if (warcRecord instanceof WarcResponse response) {
                this.warcResponse = response;
                if (response.http().status() != 200) continue; // ignore error responses
                if (response.payload().isEmpty()) continue;
                try {
                    if (response.target().startsWith("https://api.twitter.com/2/search/adaptive.json?")) {
                        return SocialJson.mapper.readValue(response.payload().get().body().stream(), AdaptiveSearch.class).posts();
                    } else if (TWEETS_AND_REPLIES_RE.matcher(response.target()).matches()) {
                        return SocialJson.mapper.readValue(response.payload().get().body().stream(), TimelineV2.Response.class).posts();
                    }
                } catch (JsonMappingException e) {
                    log.warn("Error parsing {} @ {}", warcFilename, warcReader.position(), e);
                }
            }
        }
    }

    public WarcResponse warcResponse() {
        return this.warcResponse;
    }

    public static void main(String[] args) throws IOException {
        for (var arg : args) {
            Path path = Path.of(arg);
            try (var reader = new SocialReader(new WarcReader(path))) {
                while (true) {
                    var batch = reader.nextBatch();
                    if (batch == null) break;
                    for (var post : batch) {
                        System.out.println(path.getFileName() + " " + post.createdAt() + " " + post.url() + " "
                                + post.author().username() + ": "
                                + post.content());
                    }
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        warcReader.close();
    }
}
