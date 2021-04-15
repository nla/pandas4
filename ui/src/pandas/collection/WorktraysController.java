package pandas.collection;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.core.Individual;
import pandas.core.IndividualRepository;
import pandas.core.NotFoundException;
import pandas.core.UserService;
import pandas.gather.InstanceRepository;
import pandas.report.ReportRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Controller
public class WorktraysController {
    private final TitleRepository titleRepository;
    private final InstanceRepository instanceRepository;
    private final UserService userService;
    private final AgencyRepository agencyRepository;
    private final IndividualRepository individualRepository;
    private final ReportRepository reportRepository;

    public WorktraysController(TitleRepository titleRepository, InstanceRepository instanceRepository, UserService userService, AgencyRepository agencyRepository, IndividualRepository individualRepository, ReportRepository reportRepository) {
        this.titleRepository = titleRepository;
        this.instanceRepository = instanceRepository;
        this.userService = userService;
        this.agencyRepository = agencyRepository;
        this.individualRepository = individualRepository;
        this.reportRepository = reportRepository;
    }

    @ModelAttribute
    public void commonAttributes(@PathVariable(name = "alias", required = false) String alias, Model model) {
        Long agencyId = null;
        Long ownerId = null;
        Individual currentUser = userService.getCurrentUser();
        if (alias == null || alias.isBlank()) {
            Individual user = currentUser;
            alias = user.getUserid();
            ownerId = user.getId();
        } else {
            Optional<Agency> agency = agencyRepository.findByAlias(alias);
            if (agency.isPresent()) {
                agencyId = agency.get().getId();
            } else {
                ownerId = individualRepository.findByUserid(alias).orElseThrow(NotFoundException::new).getId();
            }
        }
        if (agencyId == null && ownerId == null) {
            ownerId = currentUser.getId();
        }
        model.addAttribute("alias", alias);
        model.addAttribute("agencyId", agencyId);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("dateFormat", DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault()));
        model.addAttribute("dateTimeFormat", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault()));
        model.addAttribute("agencies", agencyRepository.findAllOrdered());
    }

    @GetMapping(value = {"/", "/worktrays", "/worktrays/{alias}"})
    public String all(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Model model) {
        Pageable pageable = PageRequest.of(0, 5);
        // Selection
        nominated(agencyId, ownerId, pageable, model);
        monitored(agencyId, ownerId, pageable, model);
        // Permission
        requestPermission(agencyId, ownerId, pageable, model);
        permissionRequested(agencyId, ownerId, pageable, model);
        // Gather
        scheduling(agencyId, ownerId, pageable, model);
        scheduled(agencyId, ownerId, pageable, model);
        gathering(agencyId, ownerId, pageable, model);
        // Preserve
        upload(agencyId, ownerId, pageable, model);
        gathered(agencyId, ownerId, pageable, model);
        // Publish
        archived(agencyId, ownerId, pageable, model);
        // Catalogue
        cataloguing(agencyId, ownerId, pageable, model);
        // Reports
        reportsRequested(agencyId, ownerId, pageable, model);
        reportsGenerated(agencyId, ownerId, pageable, model);
        return "worktrays/All";
    }

    @GetMapping("/worktrays/{alias}/nominated")
    public String nominated(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("nominatedTitles", titleRepository.worktrayNominated(agencyId, null, pageable));
        return "worktrays/Nominated";
    }

    @GetMapping("/worktrays/{alias}/monitored")
    public String monitored(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("monitoredTitles", titleRepository.worktrayMonitored(agencyId, ownerId, pageable));
        return "worktrays/Monitored";
    }

    @GetMapping("/worktrays/{alias}/reqeust-permission")
    public String requestPermission(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("requestPermissionTitles", titleRepository.worktrayPermissionRequesting(agencyId, ownerId, pageable));
        return "worktrays/RequestPermission";
    }

    @GetMapping("/worktrays/{alias}/permission-requested")
    public String permissionRequested(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("permissionRequestedTitles", titleRepository.worktrayPermissionRequested(agencyId, ownerId, pageable));
        return "worktrays/PermissionRequested";
    }

    @GetMapping("/worktrays/{alias}/scheduling")
    public String scheduling(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("awaitingSchedulingTitles", titleRepository.worktrayAwaitingScheduling(agencyId, ownerId, pageable));
        return "worktrays/Scheduling";
    }

    @GetMapping("/worktrays/{alias}/scheduled")
    public String scheduled(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("scheduledTitles", titleRepository.worktrayScheduled(agencyId, ownerId, Instant.now().plus(30, ChronoUnit.DAYS), pageable));
        return "worktrays/Scheduled";
    }

    @GetMapping("/worktrays/{alias}/gathering")
    public String gathering(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("gatheringInstances", instanceRepository.worktrayGathering(agencyId, ownerId, pageable));
        return "worktrays/Gathering";
    }

    @GetMapping("/worktrays/{alias}/upload")
    public String upload(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("instancesForUpload", instanceRepository.worktrayInstancesForUpload(agencyId, ownerId, pageable));
        return "worktrays/Upload";
    }

    @GetMapping("/worktrays/{alias}/gathered")
    public String gathered(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("gatheredInstances", instanceRepository.worktrayGathered(agencyId, ownerId, pageable));
        return "worktrays/Gathered";
    }

    @GetMapping("/worktrays/{alias}/archived")
    public String archived(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("archivedTitles", titleRepository.worktrayArchivedTitles(agencyId, ownerId, pageable));
        return "worktrays/Archived";
    }

    @GetMapping("/worktrays/{alias}/cataloguing")
    public String cataloguing(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("titlesAwaitingCataloguing", titleRepository.worktrayAwaitingCataloguing(agencyId, ownerId, pageable));
        return "worktrays/Cataloguing";
    }

    @GetMapping("/worktrays/{alias}/reports-requested")
    public String reportsRequested(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("requestedReports", reportRepository.worktrayRequested(agencyId, ownerId, pageable));
        return "worktrays/ReportsRequested";
    }

    @GetMapping("/worktrays/{alias}/reports-generated")
    public String reportsGenerated(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("generatedReports", reportRepository.worktrayGenerated(agencyId, ownerId, pageable));
        return "worktrays/ReportsGenerated";
    }
}
