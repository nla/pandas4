package pandas.gatherer.core;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;

@Controller
public class CoreController {
    private final GatherManager gatherManager;

    public CoreController(GatherManager gatherManager) {
        this.gatherManager = gatherManager;
    }

    @GetMapping("/")
    @ResponseBody
    public String home() {
        String action = gatherManager.isPaused() ? "unpause" :  "pause";
        return "pandas-gatherer " + (gatherManager.isPaused() ? "pausing new gathers" : "running") +
                "<br><form action=" + action + " method=post><button type=submit>" + action + " new gathers</button></form>" +
                "<br>Currently gathering titles: " + gatherManager.getCurrentTitles();
    }

    @PostMapping("/pause")
    public ResponseEntity<Object> pause(ServerWebExchange exchange) {
        gatherManager.setPaused(true);
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create("./")).body(null);
    }

    @PostMapping("/unpause")
    public ResponseEntity<Object> unpause(ServerWebExchange exchange) {
        gatherManager.setPaused(false);
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create("./")).body(null);
    }

    @GetMapping(value = "/version", produces = "text/plain")
    @ResponseBody
    public String version() {
        return gatherManager.version();
    }
}
