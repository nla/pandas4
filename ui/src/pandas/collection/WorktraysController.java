package pandas.collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import pandas.agency.*;
import pandas.core.NotFoundException;
import pandas.gather.Instance;
import pandas.gather.InstanceRepository;
import pandas.gather.PreviousGather;
import pandas.report.ReportRepository;

import jakarta.servlet.http.HttpSession;
import pandas.search.FacetEntry;
import pandas.search.FacetResults;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class WorktraysController {
    private static final String LAST_ALIAS = WorktraysController.class.getName() + ".lastAlias";

    private final TitleRepository titleRepository;
    private final InstanceRepository instanceRepository;
    private final UserService userService;
    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    public WorktraysController(TitleRepository titleRepository, InstanceRepository instanceRepository, UserService userService, AgencyRepository agencyRepository, UserRepository userRepository, ReportRepository reportRepository) {
        this.titleRepository = titleRepository;
        this.instanceRepository = instanceRepository;
        this.userService = userService;
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
    }

    @ModelAttribute
    public void commonAttributes(@PathVariable(name = "alias", required = false) String alias, Model model,
                                 HttpSession session) {
        // Make the worktrays sticky by stashing the alias in the session.
        if (alias == null || alias.isBlank()) {
            alias = (String) session.getAttribute(LAST_ALIAS);
        } else {
            session.setAttribute(LAST_ALIAS, alias);
        }

        User currentUser = userService.getCurrentUser();
        Long agencyId = null;
        Long ownerId = null;
        if (alias == null || alias.isBlank()) {
            alias = currentUser.getUserid();
            ownerId = currentUser.getId();
        } else {
            Optional<Agency> agency = agencyRepository.findByAlias(alias);
            if (agency.isPresent()) {
                agencyId = agency.get().getId();
            } else {
                ownerId = userRepository.findByUserid(alias).orElseThrow(NotFoundException::new).getId();
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

    @GetMapping(value = {"/worktrays", "/worktrays/{alias}"})
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
        gathered(agencyId, ownerId, Set.of(), Set.of(), pageable, model);
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
        // nominated worktray always displays all titles from their agency regardless of owner
        if (agencyId == null && ownerId != null) {
            agencyId = userRepository.findById(ownerId).orElseThrow().getAgency().getId();
        }
        model.addAttribute("nominatedTitles", titleRepository.worktrayNominated(agencyId, null, pageable));
        return "worktrays/Nominated";
    }

    @GetMapping("/worktrays/{alias}/monitored")
    public String monitored(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId, Pageable pageable, Model model) {
        model.addAttribute("monitoredTitles", titleRepository.worktrayMonitored(agencyId, ownerId, pageable));
        return "worktrays/Monitored";
    }

    @GetMapping("/worktrays/{alias}/request-permission")
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

    @GetMapping({"/worktrays/gathered", "/worktrays/{alias}/gathered"})
    public String gathered(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId,
                           @RequestParam(name = "collection", defaultValue = "") Set<Collection> collections,
                           @RequestParam(name = "problem", defaultValue = "") Set<String> problems,
                           @PageableDefault(size = 100) Pageable pageable, Model model) {
        Page<Instance> instances = instanceRepository.listGatheredWorktray(agencyId, ownerId, Pageable.unpaged());

        instances = new PageImpl<>(instances
                .filter(i -> (collections.isEmpty() || !Collections.disjoint(collections, i.getTitle().getTopLevelCollections()))
                        && (problems.isEmpty() || !Collections.disjoint(problems, i.getProblems())))
                .toList(),
                pageable, instances.getTotalElements());


        Map<Long, PreviousGather> previousGathers = new HashMap<>();
        instanceRepository.findPreviousStats(instances.getContent())
                .forEach(ps -> previousGathers.put(ps.getCurrentInstanceId(), ps));
        model.addAttribute("gatheredInstances", instances);
        model.addAttribute("previousGathers", previousGathers);

        model.addAttribute("filters", List.of(
                buildFilter("Collection", "collection", instances.toList(), collections,
                        i -> i.getTitle().getTopLevelCollections(), Collection::getId, Collection::getName),
                buildFilter("Gather Problem", "problem", instances.toList(), problems, Instance::getProblems,
                        p -> p, p -> p)
        ));

        return "worktrays/Gathered";
    }

    <T,R> FacetResults buildFilter(String name, String param, java.util.Collection<T> items,
                                   Set<R> selected, Function<T,java.util.Collection<R>> mapper,
                                   Function<R,Object> idMapper, Function<R,String> nameMapper) {
        var entries = items.stream()
                .flatMap(i -> mapper.apply(i).stream())
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()))
                .entrySet().stream().map(e -> new FacetEntry(idMapper.apply(e.getKey()).toString(),
                        nameMapper.apply(e.getKey()), e.getValue(),
                        selected.contains(e.getKey())))
                .sorted(Comparator.comparing(FacetEntry::getCount).reversed())
                .limit(10)
                .toList();
        return new FacetResults(name, param, entries, true, false, null);
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
