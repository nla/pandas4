package pandas.collection;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import pandas.agency.*;
import pandas.core.NotFoundException;
import pandas.core.Privileges;
import pandas.gather.*;
import pandas.report.ReportRepository;
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
    private final InstanceSearcher instanceSearcher;
    private final UserService userService;
    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final StateRepository stateRepository;

    public WorktraysController(TitleRepository titleRepository, InstanceRepository instanceRepository, InstanceSearcher instanceSearcher, UserService userService, AgencyRepository agencyRepository, UserRepository userRepository, ReportRepository reportRepository, StateRepository stateRepository) {
        this.titleRepository = titleRepository;
        this.instanceRepository = instanceRepository;
        this.instanceSearcher = instanceSearcher;
        this.userService = userService;
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.stateRepository = stateRepository;
    }

    @ModelAttribute
    public void commonAttributes(@PathVariable(name = "alias", required = false) String alias, Model model,
                                 HttpSession session, Authentication authentication) {
        // Make the worktrays sticky by stashing the alias in the session.
        if (alias == null || alias.isBlank()) {
            alias = (String) session.getAttribute(LAST_ALIAS);
        } else {
            session.setAttribute(LAST_ALIAS, alias);
        }

        String worktrayOwnerName;
        User currentUser = userService.getCurrentUser();
        Long agencyId = null;
        Long ownerId = null;
        if (alias == null || alias.isBlank()) {
            alias = currentUser.getUserid();
            ownerId = currentUser.getId();
            worktrayOwnerName = null;
        } else {
            Optional<Agency> agency = agencyRepository.findByAlias(alias);
            if (agency.isPresent()) {
                agencyId = agency.get().getId();
                worktrayOwnerName = agency.get().getName();
            } else {
                User user = userRepository.findByUserid(alias).orElseThrow(NotFoundException::new);
                ownerId = user.getId();
                worktrayOwnerName = currentUser.equals(user) ? null : user.getName();
            }
        }
        if (agencyId == null && ownerId == null) {
            ownerId = currentUser.getId();
        }

        if (authentication.getAuthorities().contains(Privileges.VIEW_ALL_AGENCY_WORKTRAYS)) {
            model.addAttribute("worktrayAgencies", agencyRepository.findAllOrdered());
        } else {
            model.addAttribute("worktrayAgencies", List.of(currentUser.getAgency()));
        }

        model.addAttribute("worktrayOwnerName", worktrayOwnerName);
        model.addAttribute("alias", alias);
        model.addAttribute("agencyId", agencyId);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("dateFormat", DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault()));
        model.addAttribute("dateTimeFormat", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault()));
    }

    @GetMapping(value = {"/worktrays", "/worktrays/{alias}"})
    public String all(@ModelAttribute("agencyId") Long agencyId, @ModelAttribute("ownerId") Long ownerId,
                      @RequestParam MultiValueMap<String, String> params, Model model, HttpServletRequest request) {
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
        gathered(agencyId, ownerId, params, pageable, model, request);
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
                           @RequestParam MultiValueMap<String, String> params,
                           @PageableDefault(size = 100) Pageable pageable, Model model, HttpServletRequest request) {
    	User user = userService.getCurrentUser();

        // if called without parameters, redirect to the last saved sticky parameters
        if (params.isEmpty() && user.getPrefersStickyFilters()) {
            Object attribute = request.getSession().getAttribute("qaFilterStickyParams");

            if (attribute != null) {
                return "redirect:/worktrays/gathered?" + attribute.toString();
            }
        }

        // save the parameters that we want to be sticky
        LinkedMultiValueMap<String, String> stickyParams = new LinkedMultiValueMap<String, String>(params);
        stickyParams.remove("filter");
        stickyParams.remove("q");
        stickyParams.remove("page");
        stickyParams.remove("sort");
        request.getSession().setAttribute("qaFilterStickyParams",
                UriComponentsBuilder.newInstance().queryParams(stickyParams).build().getQuery());
    	
        State gatheredState = stateRepository.findByName(State.GATHERED).orElseThrow();

        var instances = instanceSearcher.search(gatheredState.getId(), agencyId, ownerId, params, pageable);

        Map<Long, PreviousGather> previousGathers = new HashMap<>();
        instanceRepository.findPreviousStats(instances.getContent())
                .forEach(ps -> previousGathers.put(ps.getCurrentInstanceId(), ps));
        model.addAttribute("gatheredInstances", instances);
        model.addAttribute("previousGathers", previousGathers);
        model.addAttribute("filters", instances.getFacets());

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
