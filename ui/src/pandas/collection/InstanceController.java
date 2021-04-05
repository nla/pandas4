package pandas.collection;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pandas.core.UserService;
import pandas.gather.Instance;
import pandas.gather.InstanceService;
import pandas.gather.State;
import pandas.gather.StateHistoryRepository;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Controller
public class InstanceController {
    private final UserService userService;
    private final InstanceService instanceService;
    private final StateHistoryRepository stateHistoryRepository;

    public InstanceController(UserService userService, InstanceService instanceService, StateHistoryRepository stateHistoryRepository) {
        this.userService = userService;
        this.instanceService = instanceService;
        this.stateHistoryRepository = stateHistoryRepository;
    }

    @GetMapping("/instances/{id}")
    public String get(@PathVariable("id") Instance instance, Model model) {
        model.addAttribute("instance", instance);
        model.addAttribute("stateHistory", stateHistoryRepository.findByInstanceOrderByStartDate(instance));
        model.addAttribute("dateFormat", DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault()));
        return "InstanceView";
    }

    @PostMapping("/instances/{id}/delete")
    public String delete(@PathVariable("id") Instance instance) {
        if (!instance.canDelete()) throw new IllegalStateException("can't delete instance in state " + instance.getState().getName());
        instanceService.updateState(instance, State.DELETING, userService.getCurrentUser());
        return "redirect:/titles/" + instance.getTitle().getId();
    }

    @PostMapping("/instances/{id}/archive")
    public String archive(@PathVariable("id") Instance instance) {
        if (!instance.canDelete()) throw new IllegalStateException("can't archive instance in state " + instance.getState().getName());
        instanceService.updateState(instance, State.ARCHIVING, userService.getCurrentUser());
        return "redirect:/instances/" + instance.getId();
    }

    @PostMapping("/instances/{id}/stop")
    public String stop(@PathVariable("id") Instance instance) {
        if (instance.canStop()) {
            instanceService.updateState(instance, State.GATHERED, userService.getCurrentUser());
        }
        return "redirect:/instances/" + instance.getId();
    }
}
