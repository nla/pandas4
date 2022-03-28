package pandas.gather;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pandas.agency.UserService;

import java.time.*;

@Controller
public class GatherQueueController {
    private final TitleGatherRepository titleGatherRepository;
    private final InstanceRepository instanceRepository;
    private final InstanceService instanceService;
    private final UserService userService;

    public GatherQueueController(TitleGatherRepository titleGatherRepository, InstanceRepository instanceRepository, InstanceService instanceService, UserService userService) {
        this.titleGatherRepository = titleGatherRepository;
        this.instanceRepository = instanceRepository;
        this.instanceService = instanceService;
        this.userService = userService;
    }

    @GetMapping("/queue")
    public String gatherQueue(Model model) {
        Instant endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
        model.addAttribute("queuedGathers", titleGatherRepository.findQueuedBefore(endOfToday));
        model.addAttribute("gatheringInstances", instanceRepository.findGathering());
        model.addAttribute("failedInstances", instanceRepository.findFailed());
        return "gather/GatherQueue";
    }

    @PostMapping("/queue/retry")
    @Transactional
    public String retry(@RequestParam("instance") Instance instance) {
        instanceService.retryAfterFailure(instance, userService.getCurrentUser());
        return "redirect:/queue";
    }
}
