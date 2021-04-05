package pandas.gather;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.*;

@Controller
public class GatherQueueController {
    private final TitleGatherRepository titleGatherRepository;
    private final InstanceRepository instanceRepository;

    public GatherQueueController(TitleGatherRepository titleGatherRepository, InstanceRepository instanceRepository) {
        this.titleGatherRepository = titleGatherRepository;
        this.instanceRepository = instanceRepository;
    }

    @GetMapping("/queue")
    public String gatherQueue(Model model) {
        Instant endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
        model.addAttribute("queuedGathers", titleGatherRepository.findQueuedBefore(endOfToday));
        model.addAttribute("gatheringInstances", instanceRepository.findGathering());
        return "gather/GatherQueue";
    }

}
