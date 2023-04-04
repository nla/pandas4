package pandas.social;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class SocialApiController {
    private final SocialSearcher searcher;

    public SocialApiController(SocialSearcher searcher) {
        this.searcher = searcher;
    }

    @GetMapping(value = "/", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String home() {
        return "pandas-social running";
    }

    @GetMapping(value = "/search", produces = "application/json")
    @ResponseBody
    public SocialResults search(@RequestParam("q") String query,
                                @RequestParam(value = "sort", defaultValue = "relevance") String sort) throws IOException {
        return searcher.search(query, sort);
    }
}
