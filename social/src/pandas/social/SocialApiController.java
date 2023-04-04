package pandas.social;

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

    public SocialApiController(SocialIndexer indexer, SocialSearcher searcher) {
        this.indexer = indexer;
        this.searcher = searcher;
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
}
