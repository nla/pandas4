package pandas.social;

import org.netpreserve.jwarc.WarcReader;
import org.netpreserve.jwarc.WarcResponse;
import pandas.social.twitter.AdaptiveSearch;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class SocialReader implements Closeable {
    private final WarcReader warcReader;
    private WarcResponse warcResponse;

    public SocialReader(WarcReader warcReader) {
        this.warcReader = warcReader;
    }

    public List<Post> nextBatch() throws IOException {
        while (true) {
            var warcRecord = warcReader.next().orElse(null);
            if (warcRecord == null) return null;
            if (warcRecord instanceof WarcResponse response) {
                this.warcResponse = response;
                if (response.http().status() != 200) continue; // ignore error responses
                if (response.payload().isEmpty()) continue;
                if (response.target().startsWith("https://api.twitter.com/2/search/adaptive.json?")) {
                    return SocialJson.mapper.readValue(response.payload().get().body().stream(), AdaptiveSearch.class).posts();
                }
            }
        }
    }

    public WarcResponse warcResponse() {
        return this.warcResponse;
    }

    public static void main(String[] args) throws IOException {
        try (var reader = new SocialReader(new WarcReader(Paths.get(args[0])))) {
            while (true) {
                var batch = reader.nextBatch();
                if (batch == null) break;
                for (var post : batch) {
                    System.out.println(post.createdAt() + " " + post.url() + " "
                            + post.author().username() + ": "
                            + post.content());
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        warcReader.close();
    }
}
