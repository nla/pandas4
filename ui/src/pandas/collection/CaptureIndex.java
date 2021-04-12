package pandas.collection;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class CaptureIndex {
    private String baseUrl = "http://winch.nla.gov.au:9901/trove";

    public List<Capture> query(String url) {
        String qUrl = baseUrl + "?sort=reverse&url=" + URLEncoder.encode(url, UTF_8);
        try (var reader = new BufferedReader(new InputStreamReader(new URL(qUrl).openStream(), UTF_8))) {
            List<Capture> captures = new ArrayList<>();
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                captures.add(new Capture(line));
            }
            return captures;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
