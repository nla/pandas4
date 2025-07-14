package pandas.social.mastodon;

import org.netpreserve.jwarc.WarcWriter;
import org.netpreserve.jwarc.Warcinfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MastodonSiteArchiver {
    public static void main(String[] args) throws IOException {
        String userAgent = null;
        String url = null;
        String minId = "0";
        int batchSize = 40;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-A", "--user-agent" -> userAgent = args[++i];
                case "--min-id" -> minId = args[++i];
                case "--batch-size" -> batchSize = Integer.parseInt(args[++i]);
                default -> {
                    if (args[i].startsWith("-")) usage();
                    url = args[i];
                }
            }
        }
        if (url == null) {
            usage();
            return;
        }
        var metadata = Map.of("software", List.of("pandas-social"));
        try (var warcWriter = new WarcWriter(System.out)) {
            warcWriter.write(new Warcinfo.Builder().fields(metadata).build());
            var client = new MastodonClient(url, userAgent, warcWriter);
            while (true) {
                var statuses = client.getPublicTimeline(true, batchSize, minId);
                if (statuses.isEmpty()) break;
                minId = statuses.get(0).id();
            }
        }
    }

    private static void usage() {
        System.err.println("Usage: MastodonSiteArchiver [-A|--user-agent USER_AGENT] URL");
        System.exit(1);
    }
}
