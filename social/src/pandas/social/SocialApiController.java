package pandas.social;

import org.netpreserve.jwarc.WarcReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Paths;

@Controller
public class SocialApiController {
    private final SocialIndexer indexer;
    private final SocialSearcher searcher;
    private final BambooClient bambooClient;

    public SocialApiController(SocialIndexer indexer, SocialSearcher searcher, BambooClient bambooClient) {
        this.indexer = indexer;
        this.searcher = searcher;
        this.bambooClient = bambooClient;
    }

    @GetMapping(value = "/", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String home() {
        return """
            pandas-social running
            <form action=/warcs method=post>
                <input name=filename>
                <input type=submit>
            </form>
                """;
    }

    @GetMapping(value = "/search", produces = "application/json")
    @ResponseBody
    public SocialResults search(@RequestParam("q") String query,
                                @RequestParam(value = "sort", defaultValue = "relevance") String sort) throws IOException {
        return searcher.search(query, sort);
    }

    @PostMapping(value = "/warcs")
    @ResponseBody
    public String addWarc(@RequestParam("filename") String filename) throws IOException {
        indexer.addWarc(Paths.get(filename));
        return "Added " + filename;
    }

    @GetMapping("/reindex")
    @ResponseBody
    public String reindex() throws IOException {
        long warcCount = 0;
        long postCount = 0;
        for (long warcId : bambooClient.listWarcIds()) {
            try (var warcReader = new WarcReader(bambooClient.openWarc(warcId))) {
                postCount += indexer.addWarc(warcReader, Long.toString(warcId));
                warcCount++;
            }
        }
        return "Indexed " + postCount + " posts from " + warcCount + " warcs";
    }

    @GetMapping("/archiver")
    @ResponseBody
    public String archiver() {


        return "pants";
    }
}
