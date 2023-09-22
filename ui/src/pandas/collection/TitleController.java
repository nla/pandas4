package pandas.collection;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.search.engine.search.query.SearchScroll;
import org.hibernate.search.mapper.orm.Search;
import org.marc4j.MarcStreamWriter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import pandas.agency.*;
import pandas.collection.TitleSearcher.UrlCheckResult;
import pandas.core.Config;
import pandas.core.Link;
import pandas.core.View;
import pandas.gather.*;
import pandas.gatherer.CrawlBeans;
import pandas.util.DateFormats;
import pandas.util.Requests;
import pandas.util.SURT;
import pandas.util.Strings;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@Controller
public class TitleController {
    private final TitleRepository titleRepository;
    private final UserRepository userRepository;
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
    private final ScopeRepository scopeRepository;
    private final CollectionRepository collectionRepository;
    private final IssueGroupRepository issueGroupRepository;
    private final PermissionEvaluator permissionEvaluator;

    public TitleController(TitleRepository titleRepository, UserRepository userRepository, GatherMethodRepository gatherMethodRepository, GatherScheduleRepository gatherScheduleRepository, TitleService titleService, TitleSearcher titleSearcher, Config config, EntityManager entityManager, FormatRepository formatRepository, GatherService gatherService, ClassificationService classificationService, OwnerHistoryRepository ownerHistoryRepository, StatusRepository statusRepository, UserService userService, PublisherTypeRepository publisherTypeRepository, AgencyRepository agencyRepository, ProfileRepository profileRepository, Link link, ScopeRepository scopeRepository, CollectionRepository collectionRepository, IssueGroupRepository issueGroupRepository,
                           PermissionEvaluator permissionEvaluator) {
        this.titleRepository = titleRepository;
        this.userRepository = userRepository;
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
        this.scopeRepository = scopeRepository;
        this.collectionRepository = collectionRepository;
        this.issueGroupRepository = issueGroupRepository;
        this.permissionEvaluator = permissionEvaluator;
    }

    @GetMapping("/titles/{id}")
    public String get(@PathVariable("id") Title title, Model model) {
        model.addAttribute("instanceDateFormat", DateTimeFormatter.ofPattern("MMM d").withZone(ZoneId.systemDefault()));
        model.addAttribute("dateFormat", DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault()));
        model.addAttribute("title", title);
        Comparator<Integer> comparator = Comparator.naturalOrder();
        model.addAttribute("instancesByYear", title.getInstances().stream()
                .sorted(Comparator.comparing(Instance::getDate).reversed())
                .collect(groupingBy(i -> i.getDate().atZone(ZoneId.systemDefault()).getYear(),
                        () -> new TreeMap<>(comparator.reversed()), toList())));
        model.addAttribute("deletedInstancesCount", title.getInstances().stream().filter(instance -> instance.getState().isDeletedOrDeleting()).count());
        model.addAttribute("issueGroups", issueGroupRepository.findByTepTitleOrderByOrder(title));
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
        model.addAttribute("filtersNot", params.containsKey("not"));
        model.addAttribute("disappeared", params.containsKey("disappeared"));
        model.addAttribute("unableToArchive", params.containsKey("unableToArchive"));
        model.addAttribute("sort", params.getFirst("sort"));
        model.addAttribute("orderings", titleSearcher.getOrderings().keySet());
        model.addAttribute("filters", results.getFacets());
        return "TitleSearch";
    }

    @GetMapping(value = "/titles/basicSearch.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BasicSearchResult> basicSearchJson(@RequestParam("q") String q, @RequestParam("notTitle") Long notTitleId) {
        return titleSearcher.basicSearch(q, notTitleId).stream().map(BasicSearchResult::new).collect(toList());
    }

    record BasicSearchResult (long id, String name, String humanId) {
        public BasicSearchResult(Title title) {
            this(title.getId(), title.getName(), title.getHumanId());
        }
    }

    @GetMapping("/titles/bulkchange")
    public String bulkEditForm(@RequestParam MultiValueMap<String, String> params, Model model,
                               Authentication authentication) {
        List<Title> titles;
        if (params.containsKey("id")) {
            titles = new ArrayList<>();
            titleRepository.findAllById(params.get("id").stream().map(Long::parseLong).toList())
                    .forEach(titles::add);
        } else {
            var results = titleSearcher.search(params, PageRequest.of(0, config.getBulkChangeLimit()));
            titles = results.getContent();
        }

        // filter titles to only the ones the user has permission to edit
        List<Title> editableTitles = titles.stream()
                .filter(title -> permissionEvaluator.hasPermission(authentication, title, "edit"))
                .collect(Collectors.toList());

        var form = titleService.newBulkEditForm(editableTitles);
        model.addAttribute("form", form);
        model.addAttribute("allUsers", userRepository.findByActiveIsTrueOrderByNameGivenAscNameFamilyAsc());
        model.addAttribute("allGatherMethods", gatherMethodRepository.findAll());
        model.addAttribute("allGatherSchedules", gatherScheduleRepository.findAll());
        model.addAttribute("allScopes", scopeRepository.findAll());
        model.addAttribute("disallowedTitleCount", titles.size() - editableTitles.size());
        model.addAttribute("statusList", titleService.allowedStatusTransitions(editableTitles));
        return "TitleBulkEdit";
    }

    @PostMapping("/titles/bulkchange")
    public String bulkEditPerform(TitleBulkEditForm form, Model model, Authentication authentication) {
        for (var title : form.getTitles()) {
            if (!permissionEvaluator.hasPermission(authentication, title, "edit")) {
                throw new AccessDeniedException("User does not have permission to edit title " + title.getHumanId());
            }
        }

        User user = userRepository.findByUserid(authentication.getName()).orElse(null);
        titleService.bulkEdit(form, user);
        model.addAttribute("count", form.getTitles().size());
        model.addAttribute("form", form);
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

    @GetMapping("/titles/{id}/issues")
    @PreAuthorize("hasPermission(#title, 'edit')")
    public String issues(@PathVariable("id") Title title, Model model) {
        model.addAttribute("title", title);
        List<Instance> instances = new ArrayList<>(title.getInstances());
        instances.removeIf(i -> !i.getState().isArchived());
        Collections.reverse(instances);
        model.addAttribute("instances", instances);
        model.addAttribute("issueGroups", issueGroupRepository.findByTepTitleOrderByOrder(title));
        return "TitleIssues";
    }

    @PostMapping("/titles/{titleId}/issues")
    @PreAuthorize("hasPermission(#title, 'edit')")
    @Transactional
    public String updateIssues(@PathVariable("titleId") Title title,
                               @RequestParam(value = "type", required = false) List<String> types,
                               @RequestParam(value = "id", required = false) List<Long> ids,
                               @RequestParam(value = "name", required = false) ArrayDeque<String> names,
                               @RequestParam(value = "url", required = false) ArrayDeque<String> urls,
                               @RequestParam(value = "instance", required = false) ArrayDeque<Optional<Instance>> instances) {
        Tep tep = title.getTep();

        if (types == null) types = Collections.emptyList();
        if (ids == null) ids = Collections.emptyList();
        if (names == null) names = new ArrayDeque<>();
        if (instances == null) instances = new ArrayDeque<>();

        // First, build an index of all the existing groups and issues.
        Map<Long, IssueGroup> groupMap = new HashMap<>();
        Map<Long, Issue> issueMap = new HashMap<>();
        IssueGroup theNoneGroup = IssueGroup.newNoneGroup();
        for (IssueGroup group : issueGroupRepository.findByTepTitleOrderByOrder(title)) {
            if (group.isNone()) {
                theNoneGroup = group;
            } else {
                groupMap.put(group.getId(), group);
            }
            for (Issue issue : group.getIssues()) {
                issueMap.put(issue.getId(), issue);
            }
        }

        // Remove all the groups except for the -None- group.
        tep.removeAllIssueGroups();
        theNoneGroup.setOrder(0);
        theNoneGroup.removeAllIssues();
        tep.addIssueGroup(theNoneGroup);

        // Add all the new and updated groups and issues back in.
        IssueGroup group = theNoneGroup;
        for (int i = 0; i < types.size(); i++) {
            Long id = ids.get(i);
            switch (types.get(i)) {
                case "IssueGroup" -> {
                    group = id == null ? new IssueGroup() : groupMap.get(id);
                    group.setName(names.pop());
                    group.setOrder(tep.getIssueGroups().size());
                    group.removeAllIssues();
                    tep.addIssueGroup(group);
                    // If this a new group we need to save it before moving existing issues to it or we get an
                    // "object references an unsaved transient instance" error.
                    // I think this is an ordering issue where if the cascade gets to the old group before the new group
                    // it'll try to save the issue even though it's no longer in the old group.
                    if (group.getId() == null) {
                        group = issueGroupRepository.save(group);
                    }
                }
                case "Issue" -> {
                    Issue issue = id == null ? new Issue() : issueMap.get(id);
                    issue.setName(names.pop());
                    issue.setUrl(urls.pop());
                    issue.setInstance(instances.pop().orElse(null));
                    issue.setOrder(group.getIssues().size());
                    group.addIssue(issue);
                }
            }
        }

        titleRepository.save(title);

        return "redirect:/titles/" + title.getId();
    }

    @GetMapping("/titles/{id}/edit")
    @PreAuthorize("hasPermission(#title, 'edit')")
    public String edit(@PathVariable("id") Title title,
                       @RequestParam(value = "setStatus", required = false) Status setStatus,
                       HttpServletRequest request,
                       Model model) {
        TitleEditForm form = titleService.editForm(title);

        if (setStatus != null && title.getStatus().isTransitionAllowed(setStatus)) {
            form.setStatus(setStatus);
        }

        String backlink = Requests.backlink();
        if (backlink != null && !model.containsAttribute("backlink")) {
            model.addAttribute("backlink", backlink);
        }

        return editForm(model, form);
    }

    @GetMapping("/titles/{id}/clone")
    @PreAuthorize("hasPermission(#title, 'edit')")
    public String clone(@PathVariable("id") Title title, Model model) {
        TitleEditForm form = titleService.editForm(title);
        form.setId(null);
        model.addAttribute("clonedFrom", title);
        return editForm(model, form);
    }

    private String editForm(Model model, TitleEditForm form) {
        if (!model.containsAttribute("created")) {
            model.addAttribute("created", null);
        }

        model.addAttribute("form", form);
        model.addAttribute("allFormats", formatRepository.findAllByOrderByName());
        model.addAttribute("allGatherMethods", gatherMethodRepository.findAll());
        model.addAttribute("allGatherSchedules", gatherService.allGatherSchedules());
        model.addAttribute("allProfiles", profileRepository.findAllByOrderByName());
        model.addAttribute("allPublisherTypes", publisherTypeRepository.findAll());
        model.addAttribute("allScopes", scopeRepository.findAll());
        model.addAttribute("allSubjects", classificationService.allSubjects());

        var suggestedCollection = collectionRepository.findRecentlyUsed(userService.getCurrentUser(), PageRequest.ofSize(50));
        suggestedCollection = new ArrayList<>(suggestedCollection);
        suggestedCollection.removeIf(form.getCollections()::contains);
        model.addAttribute("suggestedCollections", suggestedCollection);

        if (form.getStatus() == null) {
            model.addAttribute("statusList", statusRepository.findAllById(
                    List.of(Status.NOMINATED_ID, Status.SELECTED_ID, Status.MONITORED_ID, Status.REJECTED_ID)));
        } else {
            var statusList = new ArrayList<Status>();
            statusList.add(form.getStatus());
            List<Long> statusIds = Status.allowedTransitions.getOrDefault(form.getStatus().getId(), emptyList());
            statusRepository.findAllById(statusIds).forEach(statusList::add);
            statusList.sort(Comparator.comparing(Status::getId));
            model.addAttribute("statusList", statusList);
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
                               @RequestParam(value = "newAgency", required = false) Agency newAgency, Principal principal, Model model) {
        User newOwner;
        if (newAgency == null) {
            newOwner = userRepository.findByUserid(principal.getName()).orElse(title.getOwner());
            newAgency = newOwner.getAgency();
        } else {
            newOwner = null;
        }
        model.addAttribute("title", title);
        model.addAttribute("allAgencies", agencyRepository.findAllOrdered());
        model.addAttribute("agencyUsers", userRepository.findActiveUsersByAgency(newAgency));
        model.addAttribute("newAgency", newAgency);
        model.addAttribute("newOwner", newOwner);
        return "TitleTransfer";
    }

    @PostMapping("/titles/{id}/transfer")
    @PreAuthorize("hasPermission(#title, 'edit')")
    public String transfer(@PathVariable("id") Title title, @RequestParam("newAgency") Agency newAgency,
                           @RequestParam("newOwner") User newOwner, @RequestParam("note") String note,
                           Principal principal) {
        User currentUser = userRepository.findByUserid(principal.getName()).orElse(null);
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

    @PostMapping(value = "/titles/{id}/flag")
    @PreAuthorize("isAuthenticated()")
    public String flag(@PathVariable("id") Title title,
                       @RequestParam(defaultValue = "true") boolean redirect) {
        userService.getCurrentUser().flagTitle(title);
        return redirectBackOrBlank(redirect);
    }

    @PostMapping(value = "/titles/{id}/unflag")
    @PreAuthorize("isAuthenticated()")
    public String unflag(@PathVariable("id") Title title,
                          @RequestParam(defaultValue = "true") boolean redirect) {
        userService.getCurrentUser().unflagTitle(title);
        return redirectBackOrBlank(redirect);
    }

    private static String redirectBackOrBlank(boolean shouldRedirect) {
        if (!shouldRedirect) return "_blank";
        String backlink = Requests.backlink();
        if (backlink == null) return "_blank";
        return "redirect:" + backlink;
    }

    @GetMapping("/titles/new")
    public String newForm(@RequestParam(value = "collection", required = false) Set<Collection> collections,
                          @RequestParam(value = "subject", required = false) Set<Subject> subjects,
                          @RequestParam(value = "publisher", required = false) Publisher publisher,
                          @RequestParam(value = "gatherNow", defaultValue = "false") boolean gatherNow,
                          @RequestParam(value = "name", required = false) String name,
                          @RequestParam(value = "url", required = false) String url,
                          @RequestParam(value = "publisherAbn", required = false) String publisherAbn,
                          @RequestParam(value = "publisherType", required = false) String publisherType,
                          @RequestParam(value = "created", required = false) Title created,
                          @RequestParam(value = "backlink", required = false) String backlink,
                          Model model) {

        if (collections == null) collections = new HashSet<>();
        if (subjects == null) subjects = new HashSet<>();

        // If the user pressed "Save and add another" copy the subjects & collections to the new form
        model.addAttribute("created", created);
        if (created != null) {
            subjects = created.getSubjects();
            collections = created.getCollections();
            if (backlink != null && backlink.startsWith("/publishers/") && publisher == null) {
                publisher = created.getPublisher();
            }
        }

        TitleEditForm form = titleService.newTitleForm(collections, subjects);
        form.setSeedUrls(url);
        form.setPublisherAbn(publisherAbn);

        // Copy basic gather settings to the new form when using "Save and add another"
        if (created != null) {
            form.setGatherSchedule(created.getGather().getSchedule());
            form.setGatherMethod(created.getGather().getMethod());
            form.setActiveProfile(created.getGather().getActiveProfile());
            form.setScope(created.getGather().getScope());
        }

        if (gatherNow) form.getOneoffDates().add(Instant.now());
        if (name != null) form.setName(name);

        if (publisher != null) {
            form.setPublisher(publisher);
        }

        if (publisherType != null) {
            form.setPublisherType(publisherTypeRepository.findByName(publisherType).orElse(null));
        }

        if (backlink == null) {
            if (collections.size() == 1) {
                backlink = "/collections/" + collections.iterator().next().getId();
            } else if (subjects.size() == 1) {
                backlink = "/subjects/" + subjects.iterator().next().getId();
            } else if (publisher != null) {
                backlink = "/publishers/" + publisher.getId();
            } else {
                backlink = "";
            }
        }
        model.addAttribute("backlink", backlink);

        return editForm(model, form);
    }

    @GetMapping("/titles/bulkadd")
    public String bulkAddForm(@RequestParam("collection") Collection collection, Model model) {
        model.addAttribute("collection", collection);
        return "TitleBulkAdd";
    }

    @PostMapping("/titles/bulkadd")
    @Transactional
    public String bulkAdd(
            @RequestParam("collection") Collection collection,
            @RequestParam(value = "gatherNow", defaultValue = "false") boolean gatherNow,
            @RequestParam("url") List<String> urls,
            @RequestParam("name") List<String> names,
            @RequestParam("publisherName") List<String> publisherNames,
            @RequestParam("publisherType") List<PublisherType> publisherTypes) {
        User currentUser = userService.getCurrentUser();
        for (int i = 0; i < urls.size(); i++) {
            var form = titleService.newTitleForm(Set.of(collection), null);
            form.setSeedUrls(urls.get(i));
            form.setName(names.get(i));
            form.setPublisherName(publisherNames.get(i));
            form.setPublisherType(publisherTypes.get(i));
            if (gatherNow) form.getOneoffDates().add(Instant.now());
            titleService.save(form, currentUser);
        }
        return "redirect:/collections/" + collection.getId();
    }

    @PostMapping(value = "/titles", produces = "application/json")
    public String update(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "backlink", required = false) String backlink,
            @Valid TitleEditForm form) {
        Title title = titleService.save(form, userService.getCurrentUser());
        if (action != null && action.equals("saveAndAddAnother")) {
            String target = "redirect:/titles/new?created=" + title.getId();
            if (backlink != null) {
                target += "&backlink=" + URLEncoder.encode(backlink, UTF_8);
            }
            return target;
        } else if (backlink != null && backlink.startsWith("/")) {
            return "redirect:" + backlink;
        } else {
            return "redirect:/titles/" + title.getId();
        }
    }

    @GetMapping(value = "/titles/check", produces = "application/json")
    @CrossOrigin("*")
    @ResponseBody
    public List<UrlCheckResult> checkUrl(@RequestParam("url") String url) {
        String urlWithoutPath = url.replaceFirst("^(https?://[^/]+/).*$", "$1");
        return titleSearcher.urlCheck(urlWithoutPath);
    }

    @PostMapping(value = "/titles/check", produces = "application/json")
    @ResponseBody
    public List<List<UrlCheckResult>> checkUrlBulk(@RequestParam("url") List<String> urls) {
        return urls.stream()
                .parallel()
                .map(url -> {
                    String urlWithoutPath = url.replaceFirst("^(https?://[^/]+/).*$", "$1");
                    return titleSearcher.urlCheck(urlWithoutPath);
                })
                .toList();
    }

    @GetMapping(value = "/titles/check-name", produces = "application/json")
    @CrossOrigin("*")
    @ResponseBody
    @JsonView(View.Summary.class)
    public List<Title> checkName(@RequestParam("name") String name) {
        return titleSearcher.nameCheck(name);
    }

    @PostMapping(value = "/titles/check-surts", produces = "text/plain")
    @ResponseBody
    public String checkSurts(@RequestParam("seedUrls") String seedUrls) {
        Set<String> set = new TreeSet<>();
        for (String seed: seedUrls.split("\\s+")) {
            if (Strings.isNullOrBlank(seed)) continue;
            try {
                set.add(SURT.prefixFromPlainForceHttp(seed));
            } catch (Exception e) {
                set.add(" Error: " + e + " in seed " + seed);
            }
        }
        return String.join("\n", set);
    }

    public static class ChartData {
        public final List<String> labels = new ArrayList<>();
        public final List<ChartDataset> datasets = new ArrayList<>();
    }

    public static class ChartDataset {
        public final String yAxisID;
        public final String label;
        public final String borderColor;
        public final String backgroundColor;
        public final List<Number> data = new ArrayList<>();

        public ChartDataset(String yAxisID, String label, String color) {
            this.yAxisID = yAxisID;
            this.label = label;
            this.borderColor = color;
            this.backgroundColor = color;
        }
    }

    @GetMapping(value = "/titles/{id}/charts/gathersize.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ChartData chartGatherSize(@PathVariable("id") Title title) {
        var chartData = new ChartData();
        var filesDataSet = new ChartDataset("y1files", "Files", "rgb(74, 182, 235)");
        var sizeDataSet = new ChartDataset("y2size", "Size", "rgb(25, 70, 235)");
        chartData.datasets.add(filesDataSet);
        chartData.datasets.add(sizeDataSet);
        for (var instance : tail(title.getInstances(), 25)) {
            if (instance.getState().isDeletedOrDeleting()) continue;
            if (instance.getGather() == null || instance.getGather().getSize() == null) continue;
            chartData.labels.add(DateFormats.SHORT_DATE.format(instance.getDate()).replace(".", ""));
            filesDataSet.data.add(instance.getGather().getFiles());
            sizeDataSet.data.add(instance.getGather().getSize() / 1024 / 1024);
        }
        return chartData;
    }

    public static <T> List<T> tail(List<T> list, int n) {
        return list.subList(Math.max(list.size() - n, 0), list.size());
    }
}
