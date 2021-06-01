package pandas.discovery;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.netpreserve.urlcanon.Canonicalizer;
import org.netpreserve.urlcanon.ParsedUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class DiscoverySpider {
    private static final Logger log = LoggerFactory.getLogger(DiscoverySpider.class);

    private final Queue<String> queue = new ArrayDeque<>();
    private final Set<String> seen = new HashSet<>();
    private final int delayMillis = 1000;

    private final DiscoverySource source;
    private final Consumer<Discovery> sink;

    public DiscoverySpider(DiscoverySource source, Consumer<Discovery> sink) {
        this.source = source;
        this.sink = sink;
        enqueue(source.getUrl());
    }

    private static String canonicalize(String url) {
        ParsedUrl parsed = ParsedUrl.parseUrl(url);
        Canonicalizer.AGGRESSIVE.canonicalize(parsed);
        return parsed.toString();
    }

    public void enqueue(String url) {
        String canon = canonicalize(url);
        if (seen.contains(canon)) return;
        queue.add(url);
        seen.add(canon);
    }

    @SuppressWarnings("BusyWait")
    public void run() throws IOException, InterruptedException {
        while (!queue.isEmpty()) {
            String url = queue.remove();
            log.info("Visiting {}", url);
            visit(url);
            Thread.sleep(delayMillis);
        }
    }

    private static String limit(String s) {
        if (s == null) return null;
        if (s.length() <= 1023) return s;
        return s.substring(0, 1023);
    }

    private void visit(String url) throws IOException {
        var doc = Jsoup.connect(url).maxBodySize(10 * 1024 * 1024).get();
        for (var item : doc.select(source.getItemQuery())) {
            var discovery = new Discovery();
            discovery.setSource(source);
            discovery.setSourceUrl(url);
            Element nameElement = source.getItemNameQuery() == null ? item : item.selectFirst(source.getItemNameQuery());
            discovery.setName(limit(nameElement.text()));
            Element urlElement = source.getItemLinkQuery() == null ? item : item.selectFirst(source.getItemLinkQuery());
            discovery.setUrl(limit(urlElement.attr("abs:href")));
            if (source.getItemDescriptionQuery() != null) {
                discovery.setDescription(limit(item.select(source.getItemDescriptionQuery()).text()));
            }
            sink.accept(discovery);
        }

        if (source.getItemLinkQuery() != null) {
            for (var link : doc.select(source.getLinkQuery())) {
                enqueue(link.attr("abs:href"));
            }
        }
    }
}
