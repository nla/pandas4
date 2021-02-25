package pandas.gatherer.core;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CoreController {
    private final GatherManager gatherManager;

    public CoreController(GatherManager gatherManager) {
        this.gatherManager = gatherManager;
    }

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return "pandas-gatherer running";
    }

    @GetMapping(value = "/version", produces = "text/plain")
    @ResponseBody
    public String version() {
        return gatherManager.version();
    }
}
