package pandas.social.mastodon;

import org.netpreserve.jwarc.FetchOptions;
import org.netpreserve.jwarc.WarcWriter;

import java.io.IOException;
import java.net.URI;

public class MastodonStreamArchiver {
    private final URI url;
    private final String userAgent;
    private final WarcWriter warcWriter;

    public MastodonStreamArchiver(URI url, String userAgent, WarcWriter warcWriter) {
        this.url = url;
        this.userAgent = userAgent;
        this.warcWriter = warcWriter;
    }

    void run() throws IOException {
        warcWriter.fetch(url, new FetchOptions());
    }

    public static void main(String[] args) throws IOException {
        new MastodonStreamArchiver(URI.create(args[0]), "test", new WarcWriter(System.out)).run();
    }
}
