package pandas.gather;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pandas.agency.UserService;

import java.time.*;
import java.util.List;

import static pandas.gather.State.*;

@Controller
public class GatherQueueController {
    private final TitleGatherRepository titleGatherRepository;
    private final InstanceRepository instanceRepository;
    private final InstanceService instanceService;
    private final StateRepository stateRepository;
    private final UserService userService;

    public GatherQueueController(TitleGatherRepository titleGatherRepository, InstanceRepository instanceRepository, InstanceService instanceService, StateRepository stateRepository, UserService userService) {
        this.titleGatherRepository = titleGatherRepository;
        this.instanceRepository = instanceRepository;
        this.instanceService = instanceService;
        this.stateRepository = stateRepository;
        this.userService = userService;
    }

    @GetMapping("/queue")
    public String gatherQueue(Model model) {
        Instant endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
        var gatheringStates = stateRepository.findByNameIn(List.of(GATHERING, GATHER_PAUSE, GATHER_PROCESS,
                ARCHIVING, DELETING));
        var failedStates = stateRepository.findByNameIn(List.of(FAILED));
        model.addAttribute("queuedGathers", titleGatherRepository.findQueuedBefore(endOfToday));
        model.addAttribute("gatheringInstances", instanceRepository.findByStateInOrderByDate(gatheringStates));
        model.addAttribute("failedInstances", instanceRepository.findByStateInOrderByDate(failedStates));
        return "gather/GatherQueue";
    }

    @PostMapping("/queue/retry")
    @Transactional
    public String retry(@RequestParam("instance") Instance instance) {
        instanceService.retryAfterFailure(instance, userService.getCurrentUser());
        return "redirect:/queue";
    }

    @PostMapping("/queue/retry-all")
    @Transactional
    public String retryAll() {
        for (Instance instance : instanceRepository.findFailed()) {
            instanceService.retryAfterFailure(instance, userService.getCurrentUser());
        }
        return "redirect:/queue";
    }

}
