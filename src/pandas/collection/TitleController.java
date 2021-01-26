package pandas.collection;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.search.engine.search.query.SearchScroll;
import org.hibernate.search.mapper.orm.Search;
import org.marc4j.MarcStreamWriter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import pandas.Config;
import pandas.core.Individual;
import pandas.core.IndividualRepository;
import pandas.gather.GatherMethod;
import pandas.gather.GatherMethodRepository;
import pandas.gather.GatherSchedule;
import pandas.gather.GatherScheduleRepository;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@Controller
public class TitleController {
    private final TitleRepository titleRepository;
    private final IndividualRepository individualRepository;
    private final GatherMethodRepository gatherMethodRepository;
    private final GatherScheduleRepository gatherScheduleRepository;
    private final TitleSearcher titleSearcher;
    private final Config config;
    private final EntityManager entityManager;

    public TitleController(TitleRepository titleRepository, IndividualRepository individualRepository, GatherMethodRepository gatherMethodRepository, GatherScheduleRepository gatherScheduleRepository, TitleSearcher titleSearcher, Config config, EntityManager entityManager) {
        this.titleRepository = titleRepository;
        this.individualRepository = individualRepository;
        this.gatherMethodRepository = gatherMethodRepository;
        this.gatherScheduleRepository = gatherScheduleRepository;
        this.titleSearcher = titleSearcher;
        this.config = config;
        this.entityManager = entityManager;
    }

    @GetMapping("/titles/{id}")
    public RedirectView get(@PathVariable("id") long id) {
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
        var results = titleSearcher.search(params, PageRequest.of(0, 1000));
        model.addAttribute("results", results);
        model.addAttribute("allUsers", individualRepository.findByUseridIsNotNull());
        model.addAttribute("allGatherMethods", gatherMethodRepository.findAll());
        model.addAttribute("allGatherSchedules", gatherScheduleRepository.findAll());
        return "TitleBulkEdit";
    }

    @PostMapping("/titles/bulkchange")
    public String bulkEditSummary(@RequestParam("id") List<Long> titleIds,
                                  @RequestParam("method") Long methodId,
                                  @RequestParam("schedule") Long scheduleId,
                                  @RequestParam("owner") Long ownerId,
                                  @RequestParam("note") String note, Model model) {
        GatherMethod gatherMethod = methodId == null ? null : gatherMethodRepository.findById(methodId).orElseThrow();
        GatherSchedule gatherSchedule = scheduleId == null ? null : gatherScheduleRepository.findById(scheduleId).orElseThrow();
        Individual owner = ownerId == null ? null : individualRepository.findById(ownerId).orElseThrow();
        Iterable<Title> titles = titleRepository.findAllById(titleIds);
        long count = 0;
        for (Title title : titles) {
            if (gatherMethod != null) title.getGather().setMethod(gatherMethod);
            if (gatherSchedule != null) title.getGather().setSchedule(gatherSchedule);
            if (owner != null) title.setOwner(owner);

            if (note != null && !note.isBlank()) {
                String notes = title.getNotes();
                if (notes == null) {
                    notes = "";
                } else if (!notes.endsWith("\n")) {
                    notes += "\n";
                }
                notes += note;
                title.setNotes(notes);
            }
            count++;
        }
        titleRepository.saveAll(titles);
        model.addAttribute("count", count);
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
    public String reindex(@RequestParam(name = "purge") boolean purge) throws InterruptedException {
        Search.session(entityManager).massIndexer(Title.class)
                .startAndWait();
        return "ok";
    }

    @GetMapping("/titles/new")
    public String newForm() {
        return "TitleNew.html";
    }
}
