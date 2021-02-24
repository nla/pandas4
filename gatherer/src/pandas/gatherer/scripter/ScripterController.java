package pandas.gatherer.scripter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import pandas.gather.Instance;
import pandas.gather.InstanceRepository;
import pandas.gatherer.core.WorkingArea;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class ScripterController {
    private final InstanceRepository instanceRepository;
    private final WorkingArea workingArea;

    public ScripterController(InstanceRepository instanceRepository, WorkingArea workingArea) {
        this.instanceRepository = instanceRepository;
        this.workingArea = workingArea;
    }

    @PostMapping("/replaceAllInInstance")
    public String replaceAllInInstance(GlobalReplaceRequest request) {
        Instance instance = instanceRepository.findById(request.instanceId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No such instance"));
        request.pi = instance.getPi();
        request.root = workingArea.getInstanceDir(instance.getPi(), instance.getDateString());
        return "global replacement queued.";
    }
}
