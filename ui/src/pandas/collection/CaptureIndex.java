package pandas.collection;

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
    private String baseUrl = "http://winch.nla.gov.au:9901/trove";

    public List<Capture> query(String q) {
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
        captures.sort(Comparator.comparing(Capture::getUrl)
                .thenComparing(Comparator.comparing(Capture::getDate).reversed()));
        return captures;
    }
}
