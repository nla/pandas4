package pandas.collection;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pandas.agency.AgencyRepository;
import pandas.core.UserService;
import pandas.gather.InstanceRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Controller
public class WorktraysController {
    private final TitleRepository titleRepository;
    private final InstanceRepository instanceRepository;
    private final UserService userService;
    private final AgencyRepository agencyRepository;

    public WorktraysController(TitleRepository titleRepository, InstanceRepository instanceRepository, UserService userService, AgencyRepository agencyRepository) {
        this.titleRepository = titleRepository;
        this.instanceRepository = instanceRepository;
        this.userService = userService;
        this.agencyRepository = agencyRepository;
    }

    @GetMapping("/")
    public String get(@RequestParam(value = "agency", required = false) Long agencyId,
                      @RequestParam(value = "owner", required = false) Long ownerId,
                      Model model) {
        Pageable pageable = PageRequest.of(0, 5);
        if (agencyId == null && ownerId == null) {
            ownerId = userService.getCurrentUser().getId();
        }
        model.addAttribute("dateFormat", DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault()));
        model.addAttribute("dateTimeFormat", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault()));
        model.addAttribute("selectedAgencyId", agencyId);
        model.addAttribute("agencies", agencyRepository.findAllOrdered());
        model.addAttribute("nominatedTitles", titleRepository.worktrayNominated(agencyId, null, pageable));
        model.addAttribute("monitoredTitles", titleRepository.worktrayMonitored(agencyId, ownerId, pageable));
        model.addAttribute("awaitingSchedulingTitles", titleRepository.worktrayAwaitingScheduling(agencyId, ownerId, pageable));
        model.addAttribute("scheduledTitles", titleRepository.worktrayScheduled(agencyId, ownerId, Instant.now().plus(30, ChronoUnit.DAYS), pageable));
        model.addAttribute("gatheringInstances", instanceRepository.worktrayGathering(agencyId, ownerId, pageable));
        model.addAttribute("instancesForUpload", instanceRepository.worktrayInstancesForUpload(agencyId, ownerId, pageable));
        model.addAttribute("gatheredInstances", instanceRepository.worktrayGathered(agencyId, ownerId, pageable));
        model.addAttribute("archivedTitles", titleRepository.worktrayArchivedTitles(agencyId, ownerId, pageable));
        model.addAttribute("titlesAwaitingCataloguing", titleRepository.worktrayAwaitingCataloguing(agencyId, ownerId, pageable));
        return "Worktrays";
    }
}
