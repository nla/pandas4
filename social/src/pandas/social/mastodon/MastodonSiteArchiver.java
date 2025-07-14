package pandas.social.mastodon;

import org.netpreserve.jwarc.WarcWriter;
import org.netpreserve.jwarc.Warcinfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class MastodonSiteArchiver {
    public static void main(String[] args) throws IOException {
        String userAgent = null;
        String url = null;
        String minId = "0";
        int batchSize = 40;
        Path outputPath = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-A", "--user-agent" -> userAgent = args[++i];
                case "-h", "--help" -> usage();
                case "--min-id" -> minId = args[++i];
                case "--batch-size" -> batchSize = Integer.parseInt(args[++i]);
                case "-o", "--output" -> outputPath = Path.of(args[++i]);
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
        try (var warcWriter = outputPath == null ? new WarcWriter(System.out) : new WarcWriter(outputPath)) {
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
        System.err.println("  -A, --user-agent USER_AGENT  User agent to use for requests");
        System.err.println("  -h, --help                   Show this help message");
        System.err.println("  -o, --output FILE            Write output to FILE instead of stdout");
        System.err.println("  --min-id MIN_ID              Minimum ID to fetch (default 0)");
        System.err.println("  --batch-size BATCH_SIZE      Number of statuses to fetch per request (default 40)");
        System.err.println("  URL                          URL of the Mastodon server");
        System.exit(1);
    }
}
