package pandas.collection;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

@Controller
public class UrlStatsController {
    private final UrlStatsRepository urlStatsRepository;

    public UrlStatsController(UrlStatsRepository urlStatsRepository) {
        this.urlStatsRepository = urlStatsRepository;
    }

    @GetMapping("/urlstats/raw")
    public void raw(UrlStats query, ServletResponse response) throws IOException {
        response.setContentType("text/plain");
        try (Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            for (UrlStats row : urlStatsRepository.findAll(Example.of(query))) {
                writer.write(row.toString());
            }
        }
    }
}
