package pandas.gatherer.scripter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import pandas.gather.Instance;
import pandas.gather.InstanceRepository;
import pandas.gatherer.core.GatherManager;
import pandas.gatherer.core.WorkingArea;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller
public class ScripterController {
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final InstanceRepository instanceRepository;
    private final WorkingArea workingArea;
    private final GatherManager gatherManager;

    public ScripterController(InstanceRepository instanceRepository, WorkingArea workingArea, GatherManager gatherManager) {
        this.instanceRepository = instanceRepository;
        this.workingArea = workingArea;
        this.gatherManager = gatherManager;
    }

    @GetMapping("/replaceAllInInstance")
    @ResponseBody
    public String getReplaceAllInInstance() {
        return "POST expected";
    }

    @PostMapping(value = "/replaceAllInInstance", produces = "text/plain")
    @ResponseBody
    public String replaceAllInInstance(GlobalReplaceRequest request) {
        Instance instance = instanceRepository.findById(request.instanceId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "No such instance"));
        request.pi = instance.getPi();
        request.root = workingArea.getInstanceDir(instance.getPi(), instance.getDateString());
        threadPool.execute(request);
        return "global replacement queued.";
    }

    @GetMapping(value = "/gatherDataExists", produces = "text/plain")
    @ResponseBody
    public String gatherDataExists(@RequestParam("instanceId") long instanceId) {
        return gatherManager.isInstancePopulated(instanceId);
    }
}
