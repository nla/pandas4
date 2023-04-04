package pandas.social;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Service
public class BambooClient {

    private final long collectionId;
    private final String baseUrl;


    public BambooClient() {
        collectionId = 1;
        baseUrl = "http://localhost:8080";
    }

    public List<Long> listWarcIds() throws IOException {
        var refs = SocialJson.mapper.readValue(URI.create(baseUrl + "/collections/" + collectionId + "/warcs/json").toURL(), WarcRef[].class);
        return Arrays.stream(refs).map(ref -> ref.id).toList();
    }

    public InputStream openWarc(long warcId) throws IOException {
        return URI.create(baseUrl + "/warcs/" + warcId).toURL().openStream();
    }

    public record WarcRef (long id, Long urlCount) {
    }
}
