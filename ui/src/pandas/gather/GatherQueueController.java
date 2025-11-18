package pandas.gather;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pandas.agency.UserService;
import pandas.gatherer.BlockingTask;

import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pandas.gather.State.*;

@Controller
public class GatherQueueController {
    private final TitleGatherRepository titleGatherRepository;
    private final InstanceRepository instanceRepository;
    private final InstanceService instanceService;
    private final StateRepository stateRepository;
    private final UserService userService;
    private final GathererClient gathererClient;

    public GatherQueueController(TitleGatherRepository titleGatherRepository, InstanceRepository instanceRepository, InstanceService instanceService, StateRepository stateRepository, UserService userService,
                                 GathererClient gathererClient) {
        this.titleGatherRepository = titleGatherRepository;
        this.instanceRepository = instanceRepository;
        this.instanceService = instanceService;
        this.stateRepository = stateRepository;
        this.userService = userService;
        this.gathererClient = gathererClient;
    }

    @GetMapping("/queue")
    public String gatherQueue(Model model) {
        var statusFuture = gathererClient.statusAsync();
        var conflictsFuture = gathererClient.conflictsAsync();
        Instant endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
        var gatheringStates = List.of(GATHERING, GATHER_PAUSE, GATHER_PROCESS,
                ARCHIVING, DELETING);
        model.addAttribute("queuedGathers", titleGatherRepository.findQueuedBefore(endOfToday));
        model.addAttribute("gatheringInstances", instanceRepository.findByStateInOrderByDate(gatheringStates));
        model.addAttribute("failedInstances", listFailedInstances());
        var status = statusFuture.join().replace("pandas-gatherer ", "");
        model.addAttribute("gathererStatus", status);
        model.addAttribute("conflicts", conflictsFuture.join());
        return "gather/GatherQueue";
    }

    @PostMapping("/queue/pause")
    @PreAuthorize("hasAuthority('PRIV_CONTROL_GATHERER')")
    public String pauseGathers() {
        gathererClient.pause();
        return "redirect:/queue";
    }

    @PostMapping("/queue/unpause")
    @PreAuthorize("hasAuthority('PRIV_CONTROL_GATHERER')")
    public String unpauseGathers() {
        gathererClient.unpause();
        return "redirect:/queue";
    }

    @PostMapping("/queue/retry")
    @PreAuthorize("hasPermission(#instanceId, 'Instance', 'edit')")
    public String retry(@RequestParam("instance") long instanceId) {
        instanceService.retryAfterFailure(instanceId, userService.getCurrentUser());
        return "redirect:/queue";
    }

    @PostMapping("/queue/retry-all")
    @PreAuthorize("hasAuthority('PRIV_CONTROL_GATHERER')")
    @Transactional
    public String retryAll() {
        instanceService.retryAllFailed(userService.getCurrentUser());
        return "redirect:/queue";
    }

    @PostMapping("/queue/delete-all-failed")
    public String deleteAllFailed() {
        instanceService.deleteAllFailed(userService.getCurrentUser());
        return "redirect:/queue";
    }


    private List<Instance> listFailedInstances() {
        return instanceRepository.findByStateInOrderByDate(List.of(FAILED));
    }
}
