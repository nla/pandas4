package pandas.collection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class CaptureIndex {
    public CaptureIndex(@Value("${CDX_URL:}") String cdxUrl) {
        this.baseUrl = cdxUrl;
    }

    private String baseUrl;

    public List<Capture> query(String q, boolean excludeErrors) {
        List<String> urls = new ArrayList<>();
        Set<String> digests = new HashSet<>();
        for (String word : q.split(" ")) {
            if (word.startsWith("digest:")) {
                digests.add(word.substring("digest:".length()));
            } else {
                urls.add(word);
            }
        }

        List<Capture> captures = new ArrayList<>();
        for (String url : urls) {
            String qUrl = baseUrl + "?url=" + URLEncoder.encode(url.toString(), UTF_8);
            qUrl += "&omitSelfRedirects=false";
            if (excludeErrors) qUrl += "&filter=!status:[45]..";
            try (var reader = new BufferedReader(new InputStreamReader(new URL(qUrl).openStream(), UTF_8))) {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) break;
                    Capture capture = new Capture(line);
                    if (!digests.isEmpty() && !digests.contains(capture.getDigest())) continue;
                    captures.add(capture);
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return captures;
    }

    public List<Capture> queryDateDesc(String q) {
        var captures = query(q, false);
        captures.sort(Comparator.comparing(Capture::getUrl)
                .thenComparing(Comparator.comparing(Capture::getDate).reversed()));
        return captures;
    }

    public List<CaptureGroup> queryGrouped(String q) {
        List<CaptureGroup> groups = new ArrayList<>();
        Capture firstInGroup = null;
        Capture prev = null;
        int groupCount = 0;
        int uniqueCount = 0;
        for (Capture capture : query(q, true)) {
            if (firstInGroup == null || !capture.getUrl().equals(firstInGroup.getUrl())) {
                if (firstInGroup != null) {
                    groups.add(new CaptureGroup(firstInGroup, prev, groupCount, uniqueCount));
                }
                firstInGroup = capture;
                prev = null;
                groupCount = 0;
                uniqueCount = 0;
            }
            groupCount++;
            if (prev == null || !capture.getDigest().equals(prev.getDigest())) {
                uniqueCount++;
            }
            prev = capture;
        }
        if (firstInGroup != null) {
            groups.add(new CaptureGroup(firstInGroup, prev, groupCount, uniqueCount));
        }
        return groups;
    }
}
