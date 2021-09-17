package pandas.collection;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.search.engine.search.query.SearchScroll;
import org.hibernate.search.mapper.orm.Search;
import org.marc4j.MarcStreamWriter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.core.*;
import pandas.gather.*;
import pandas.gatherer.CrawlBeans;

import javax.persistence.EntityManager;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.REFERER;

@Controller
public class TitleController {
    private final TitleRepository titleRepository;
    private final IndividualRepository individualRepository;
    private final GatherMethodRepository gatherMethodRepository;
    private final GatherScheduleRepository gatherScheduleRepository;
    private final TitleService titleService;
    private final TitleSearcher titleSearcher;
    private final Config config;
    private final EntityManager entityManager;
    private final FormatRepository formatRepository;
    private final GatherService gatherService;
    private final ClassificationService classificationService;
    private final OwnerHistoryRepository ownerHistoryRepository;
    private final StatusRepository statusRepository;
    private final UserService userService;
    private final PublisherTypeRepository publisherTypeRepository;
    private final AgencyRepository agencyRepository;
    private final ProfileRepository profileRepository;

    public TitleController(TitleRepository titleRepository, IndividualRepository individualRepository, GatherMethodRepository gatherMethodRepository, GatherScheduleRepository gatherScheduleRepository, TitleService titleService, TitleSearcher titleSearcher, Config config, EntityManager entityManager, FormatRepository formatRepository, GatherService gatherService, ClassificationService classificationService, OwnerHistoryRepository ownerHistoryRepository, StatusRepository statusRepository, UserService userService, PublisherTypeRepository publisherTypeRepository, AgencyRepository agencyRepository, ProfileRepository profileRepository, Link link) {
        this.titleRepository = titleRepository;
        this.individualRepository = individualRepository;
        this.gatherMethodRepository = gatherMethodRepository;
        this.gatherScheduleRepository = gatherScheduleRepository;
        this.titleService = titleService;
        this.titleSearcher = titleSearcher;
        this.config = config;
        this.entityManager = entityManager;
        this.formatRepository = formatRepository;
        this.gatherService = gatherService;
        this.classificationService = classificationService;
        this.ownerHistoryRepository = ownerHistoryRepository;
        this.statusRepository = statusRepository;
        this.userService = userService;
        this.publisherTypeRepository = publisherTypeRepository;
        this.agencyRepository = agencyRepository;
        this.profileRepository = profileRepository;
    }

    @GetMapping("/titles/{id}")
    public String get(@PathVariable("id") Title title, Model model) {
        model.addAttribute("instanceDateFormat", DateTimeFormatter.ofPattern("MMM d").withZone(ZoneId.systemDefault()));
        model.addAttribute("dateFormat", DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault()));
        model.addAttribute("title", title);
        Comparator<Integer> comparator = Comparator.naturalOrder();
        model.addAttribute("instancesByYear", title.getInstances().stream().collect(
                groupingBy(i -> i.getDate().atZone(ZoneId.systemDefault()).getYear(),
                () -> new TreeMap<>(comparator.reversed()), toList())));
        model.addAttribute("deletedInstancesCount", title.getInstances().stream().filter(instance -> State.DELETED.equals(instance.getState().getName())).count());
        return "TitleView";
    }

    @GetMapping("/titles/{id}/ownerhistory")
    public String ownerHistory(@PathVariable("id") Title title, Model model) {
        model.addAttribute("title", title);
        model.addAttribute("history", ownerHistoryRepository.findByTitleOrderByDate(title));
        model.addAttribute("dateFormat", DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault()));
        model.addAttribute("timeFormat", DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault()));
        return "TitleOwnerHistory";
    }

    @GetMapping("/titles/{id}/p3")
    public RedirectView pandas3(@PathVariable("id") long id) {
        return new RedirectView(config.managementDirectActionUrl("titleView?id=" + id));
    }

    @GetMapping("/titles")
    public String search(@RequestParam MultiValueMap<String, String> params,
                         @PageableDefault(20) Pageable pageable,
                         Model model) {
        var results = titleSearcher.search(params, pageable);
        model.addAttribute("results", results);
        model.addAttribute("q", params.getFirst("q"));
        model.addAttribute("sort", params.getFirst("sort"));
        model.addAttribute("orderings", titleSearcher.getOrderings().keySet());
        model.addAttribute("facets", results.getFacets());
        return "TitleSearch";
    }

    @GetMapping("/titles/bulkchange")
    public String bulkEditForm(@RequestParam MultiValueMap<String, String> params, Model model) {
        var results = titleSearcher.search(params, PageRequest.of(0, 10000));
        var form = titleService.newBulkEditForm(results.getContent());
        model.addAttribute("form", form);
        model.addAttribute("allUsers", individualRepository.findByUseridIsNotNull());
        model.addAttribute("allGatherMethods", gatherMethodRepository.findAll());
        model.addAttribute("allGatherSchedules", gatherScheduleRepository.findAll());
        return "TitleBulkEdit";
    }

    @PostMapping("/titles/bulkchange")
    public String bulkEditPerform(TitleBulkEditForm form, Model model, Principal principal) {
        Individual user = individualRepository.findByUserid(principal.getName()).orElse(null);
        titleService.bulkEdit(form, user);
        model.addAttribute("count", form.getTitles().size());
        return "TitleBulkChangeComplete";
    }


    @GetMapping(value = "/titles.csv", produces = "text/csv")
    public void exportCsv(@RequestParam MultiValueMap<String, String> params, HttpServletResponse response) throws IOException {
        response.setHeader(CONTENT_DISPOSITION, ContentDisposition.builder("attachment")
                .filename("titles.csv").build().toString());
        try (SearchScroll<Title> scroll = titleSearcher.scroll(params);
             CSVPrinter csv = CSVFormat.DEFAULT.withHeader(
                     "PI", "Name", "Date Registered", "Agency", "Owner", "Format",
                     "Gather Method", "Gather Schedule", "Next Gather Date", "Title URL", "Seed URL",
                     "Publisher", "Publisher Type", "Subjects")
                     .print(new OutputStreamWriter(response.getOutputStream(), UTF_8))) {
            for (var chunk = scroll.next(); chunk.hasHits(); chunk = scroll.next()) {
                for (Title title : chunk.hits()) {
                    csv.print(title.getPi());
                    csv.print(title.getName());
                    csv.print(title.getRegDateLocal());
                    csv.print(title.getAgency().getOrganisation().getAlias());
                    csv.print(title.getOwner() == null ? null : title.getOwner().getUserid());
                    csv.print(title.getFormat() == null ? null : title.getFormat().getName());
                    csv.print(title.getGather() == null || title.getGather().getMethod() == null ? null : title.getGather().getMethod().getName());
                    csv.print(title.getGather() == null || title.getGather().getSchedule() == null ? null : title.getGather().getSchedule().getName());
                    csv.print(title.getGather() == null || title.getGather().getNextGatherDate() == null ? null : LocalDateTime.ofInstant(title.getGather().getNextGatherDate(), ZoneId.systemDefault()));
                    csv.print(title.getTitleUrl());
                    csv.print(title.getSeedUrl());
                    csv.print(title.getPublisher() == null ? null : title.getPublisher().getName());
                    csv.print(title.getPublisher() == null || title.getPublisher().getType() == null ? null : title.getPublisher().getType().getName());
                    csv.print(title.getSubjects().stream().map(Subject::getName).collect(joining("; ")));
                    csv.println();
                }
            }
        }
    }

    @GetMapping(value = "/titles.mrc", produces = "application/marc")
    public void exportMarc(@RequestParam MultiValueMap<String, String> params, HttpServletResponse response) throws IOException {
        try (SearchScroll<Title> scroll = titleSearcher.scroll(params)) {
            response.setHeader(CONTENT_DISPOSITION, ContentDisposition.builder("attachment")
                    .filename("titles.mrc").build().toString());
            MarcStreamWriter marc = new MarcStreamWriter(response.getOutputStream(), "UTF-8");
            Date today = new Date();
            for (var chunk = scroll.next(); chunk.hasHits(); chunk = scroll.next()) {
                for (Title title : chunk.hits()) {
                    marc.write(TitleMarcConverter.convert(title, today));
                }
            }
            marc.close();
        }
    }

    @GetMapping(value = "/titles.mrc.txt", produces = "text/plain")
    public void exportMarcText(@RequestParam MultiValueMap<String, String> params, HttpServletResponse response) throws IOException {
        try (SearchScroll<Title> scroll = titleSearcher.scroll(params)) {
            response.setHeader(CONTENT_DISPOSITION, ContentDisposition.builder("inline")
                    .filename("titles.mrc.txt").build().toString());
            OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), UTF_8);
            Date today = new Date();
            for (var chunk = scroll.next(); chunk.hasHits(); chunk = scroll.next()) {
                for (Title title : chunk.hits()) {
                    writer.write(TitleMarcConverter.convert(title, today).toString());
                    writer.write("\n");
                }
            }
        }
    }

    @GetMapping("/titles/reindex")
    @ResponseBody
    @PreAuthorize("hasAuthority('PRIV_SYSADMIN')")
    public String reindex() throws InterruptedException {
        Search.session(entityManager).massIndexer(Title.class).startAndWait();
        return "ok";
    }

    @GetMapping("/titles/{id}/edit")
    public String edit(@PathVariable("id") Title title,
                       @RequestParam(value = "setStatus", required = false) Status setStatus,
                       HttpServletRequest request,
                       Model model) {
        TitleEditForm form = titleService.editForm(title);

        if (setStatus != null && title.getStatus().isTransitionAllowed(setStatus)) {
            form.setStatus(setStatus);
        }

        model.addAttribute("form", form);
        model.addAttribute("allFormats", formatRepository.findAllByOrderByName());
        model.addAttribute("allGatherMethods", gatherMethodRepository.findAll());
        model.addAttribute("allGatherSchedules", gatherScheduleRepository.findAll());
        model.addAttribute("allProfiles", profileRepository.findAllByOrderByName());
        model.addAttribute("allPublisherTypes", publisherTypeRepository.findAll());
        model.addAttribute("allSubjects", classificationService.allSubjects());

        var statusList = new ArrayList<Status>();
        statusList.add(form.getStatus());
        List<Long> statusIds = Status.allowedTransitions.getOrDefault(form.getStatus().getId(), emptyList());
        statusRepository.findAllById(statusIds).forEach(statusList::add);
        statusList.sort(Comparator.comparing(Status::getId));
        model.addAttribute("statusList", statusList);

        String referer = request.getHeader(REFERER);
        if (referer != null) {
            String prefix = request.getRequestURL().toString().replaceFirst("(https?://[^/]+)/.*", "$1");
            prefix += request.getContextPath() + "/";
            if (referer.startsWith(prefix)) {
                model.addAttribute("backlink", referer.substring(prefix.length() - 1));
            }
        }

        return "TitleEdit";
    }

    @PostMapping("/titles/{id}/delete")
    @PreAuthorize("hasPermission(#title, 'edit')")
    public String delete(@PathVariable("id") Title title) {
        titleRepository.delete(title);
        return "redirect:/titles";
    }

    @GetMapping("/titles/{id}/transfer")
    @PreAuthorize("hasPermission(#title, 'edit')")
    public String transferForm(@PathVariable("id") Title title,
                               @RequestParam(value = "newAgency", required = false) Agency newAgency, Model model) {
        if (newAgency == null) {
            newAgency = title.getAgency();
        }
        model.addAttribute("title", title);
        model.addAttribute("newAgency", newAgency);
        model.addAttribute("allAgencies", agencyRepository.findAllOrdered());
        model.addAttribute("agencyUsers", individualRepository.findActiveUsersByAgency(newAgency));
        return "TitleTransfer";
    }

    @PostMapping("/titles/{id}/transfer")
    @PreAuthorize("hasPermission(#title, 'edit')")
    public String transfer(@PathVariable("id") Title title, @RequestParam("newAgency") Agency newAgency,
                           @RequestParam("newOwner") Individual newOwner, @RequestParam("note") String note,
                           Principal principal) {
        Individual currentUser = individualRepository.findByUserid(principal.getName()).orElse(null);
        titleService.transferOwnership(title, newAgency, newOwner, note, currentUser);
        return "redirect:/titles/" + title.getId();
    }

    @GetMapping(value = "/titles/{id}/heritrix-config", produces = "application/xml")
    public void heritrixConfig(@PathVariable("id") Title title, ServletResponse response) throws IOException {
        Instance instance = new Instance();
        instance.setTitle(title);
        instance.setDate(Instant.now());
        response.setContentType("application/xml");
        CrawlBeans.writeCrawlXml(instance, new OutputStreamWriter(response.getOutputStream(), UTF_8), null);
    }

    @GetMapping("/titles/new")
    public String newForm(@RequestParam(value = "collection", required = false) List<Collection> collections,
                          @RequestParam(value = "subject", required = false) List<Subject> subjects,
                          @RequestParam(value = "url", required = false) String url,
                          Model model) {
        TitleEditForm form = titleService.newTitleForm(collections, subjects);
        form.setTitleUrl(url);
        model.addAttribute("form", form);
        model.addAttribute("allFormats", formatRepository.findAllByOrderByName());
        model.addAttribute("allGatherMethods", gatherMethodRepository.findAll());
        model.addAttribute("allGatherSchedules", gatherService.allGatherSchedules());
        model.addAttribute("allProfiles", profileRepository.findAllByOrderByName());
        model.addAttribute("allPublisherTypes", publisherTypeRepository.findAll());
        model.addAttribute("allSubjects", classificationService.allSubjects());
        model.addAttribute("statusList", statusRepository.findAllById(
                List.of(Status.NOMINATED_ID, Status.SELECTED_ID, Status.MONITORED_ID, Status.REJECTED_ID)));

        String backlink;
        if (collections != null && collections.size() == 1) {
            backlink = "/collections/" + collections.get(0).getId();
        } else if (subjects != null && subjects.size() == 1) {
            backlink = "/subjects/" + subjects.get(0).getId();
        } else {
            backlink = "";
        }
        model.addAttribute("backlink", backlink);

        return "TitleEdit";
    }

    @PostMapping(value = "/titles", produces = "application/json")
    public String update(@RequestParam(value = "backlink", required = false) String backlink, @Valid TitleEditForm form) {
        Title title = titleService.save(form, userService.getCurrentUser());
        if (backlink != null && backlink.startsWith("/")) {
            return "redirect:" + backlink;
        } else {
            return "redirect:/titles/" + title.getId();
        }
    }

    @GetMapping(value = "/titles/check", produces = "application/json")
    @CrossOrigin("*")
    @ResponseBody
    @JsonView(View.Summary.class)
    public List<Title> check(@RequestParam("url") String url) {
        String urlWithoutPath = url.replaceFirst("^(https?://[^/]+/).*$", "$1");
        return titleSearcher.urlCheck(urlWithoutPath);
    }

}
