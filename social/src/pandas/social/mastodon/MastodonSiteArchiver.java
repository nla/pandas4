package pandas.social.mastodon;

import org.netpreserve.jwarc.WarcWriter;

import java.io.IOException;

public class MastodonSiteArchiver {
    public static void main(String[] args) throws IOException {
        String userAgent = null;
        String url = null;
        String minId = "0";
        int limit = 40;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-A", "--user-agent" -> userAgent = args[++i];
                case "--min-id" -> minId = args[++i];
                case "--limit" -> limit = Integer.parseInt(args[++i]);
                default -> {
                    if (args[i].startsWith("-")) usage();
                    url = args[i];
                }
            }
        }
        if (url == null) usage();
        try (var warcWriter = new WarcWriter(System.out)) {
            var client = new MastodonClient(url, userAgent, warcWriter);
            while (true) {
                var statuses = client.getPublicTimeline(true, limit, minId);
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
