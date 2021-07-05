package pandas.gatherer.scripter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import pandas.gather.Instance;
import pandas.gather.InstanceRepository;
import pandas.gatherer.core.WorkingArea;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller
public class ScripterController {
    private final InstanceRepository instanceRepository;
    private final WorkingArea workingArea;

    public ScripterController(InstanceRepository instanceRepository, WorkingArea workingArea) {
        this.instanceRepository = instanceRepository;
        this.workingArea = workingArea;
    }

    @GetMapping("/replaceAllInInstance")
    @ResponseBody
    public String getReplaceAllInInstance() {
        return "POST expected";
    }

    @PostMapping("/replaceAllInInstance")
    @ResponseBody
    public String replaceAllInInstance(GlobalReplaceRequest request) {
        Instance instance = instanceRepository.findById(request.instanceId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "No such instance"));
        request.pi = instance.getPi();
        request.root = workingArea.getInstanceDir(instance.getPi(), instance.getDateString());
        return "global replacement queued.";
    }
}
