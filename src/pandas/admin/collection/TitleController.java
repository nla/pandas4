package pandas.admin.collection;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchScroll;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import pandas.admin.Config;
import pandas.admin.agency.Agency;
import pandas.admin.agency.AgencyRepository;
import pandas.admin.core.Individual;
import pandas.admin.core.IndividualRepository;
import pandas.admin.gather.GatherMethod;
import pandas.admin.gather.GatherMethodRepository;
import pandas.admin.gather.GatherSchedule;
import pandas.admin.gather.GatherScheduleRepository;
import pandas.admin.search.*;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.hibernate.search.engine.search.common.BooleanOperator.AND;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@Controller
public class TitleController {
    private final TitleRepository titleRepository;
    private final Config config;
    private final EntityManager entityManager;
    private final Facet[] facets;

    public TitleController(TitleRepository titleRepository, SubjectRepository subjectRepository, AgencyRepository agencyRepository, CollectionRepository collectionRepository, FormatRepository formatRepository, StatusRepository statusRepository, GatherMethodRepository gatherMethodRepository, GatherScheduleRepository gatherScheduleRepository, IndividualRepository individualRepository, PublisherRepository publisherRepository, PublisherTypeRepository publisherTypeRepository, Config config, EntityManager entityManager) {
        this.titleRepository = titleRepository;
        this.config = config;
        this.entityManager = entityManager;
        facets = new Facet[]{
                new EntityFacet<>("Agency", "agency", "agency.id", agencyRepository::findAllById, Agency::getId, Agency::getName),
                new EntityFacet<>("Collection", "collection", "collections.id", collectionRepository::findAllById, Collection::getId, Collection::getFullName, List.of("collections.name")),
                new DateFacet("Date Registered", "regdate", "regDate"),
                new EntityFacet<>("Format", "format", "format.id", formatRepository::findAllById, Format::getId, Format::getName),
                new EntityFacet<>("Gather Method", "method", "gather.method.id", gatherMethodRepository::findAllById, GatherMethod::getId, GatherMethod::getName),
                new EntityFacet<>("Gather Schedule", "schedule", "gather.schedule.id", gatherScheduleRepository::findAllById, GatherSchedule::getId, GatherSchedule::getName),
                new DateFacet("Next Gather Date", "nextgather", "gather.nextGatherDate"),
                new EntityFacet<>("Owner", "owner", "owner.id", individualRepository::findAllById, Individual::getId, Individual::getName, List.of("owner.nameGiven", "owner.nameFamily", "owner.userid")),
                new EntityFacet<>("Publisher", "publisher", "publisher.id", publisherRepository::findAllById, Publisher::getId, Publisher::getName, List.of("publisher.organisation.name")),
                new EntityFacet<>("Publisher Type", "publisher.type", "publisher.type.id", publisherTypeRepository::findAllById, PublisherType::getId, PublisherType::getName),
                new EntityFacet<>("Status", "status", "status.id", statusRepository::findAllById, Status::getId, Status::getName),
                new EntityFacet<>("Subject", "subject", "subjects.id", subjectRepository::findAllById, Subject::getId, Subject::getName, List.of("subjects.name")),
                new EntityFacet<>("Subject 2", "subject2", "subjects.id", subjectRepository::findAllById, Subject::getId, Subject::getName, List.of("subjects.name"))
        };
    }

    @GetMapping("/titles/{id}")
    public RedirectView get(@PathVariable("id") long id) {
        return new RedirectView(config.managementDirectActionUrl("titleView?id=" + id));
    }

    @GetMapping("/titles")
    public String search(@RequestParam MultiValueMap<String, String> params,
                         @PageableDefault(20) Pageable pageable,
                         Model model) {
        TitleSearch search = new TitleSearch(entityManager, facets, params, pageable);
        var results = search.execute();

        model.addAttribute("results", results);
        model.addAttribute("q", params.getFirst("q"));
        model.addAttribute("facets", results.getFacets());
        return "TitleSearch";
    }

    @GetMapping(value = "/titles.csv", produces = "text/csv")
    public void search(@RequestParam MultiValueMap<String, String> params, HttpServletResponse response) throws IOException {
        TitleSearch search = new TitleSearch(entityManager, facets, params, Pageable.unpaged());
        response.setHeader(CONTENT_DISPOSITION, ContentDisposition.builder("attachment")
                .filename("titles.csv").build().toString());
        try (SearchScroll<Title> scroll = search.scroll();
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

    @GetMapping("/titles/reindex")
    @ResponseBody
    public String reindex() throws InterruptedException {
        Search.session(entityManager).massIndexer(Title.class).startAndWait();
        return "ok";
    }

    private static class TitleSearch {
        private final SearchSession session;
        private final Facet[] facets;
        private final MultiValueMap<String, String> params;
        private final Pageable pageable;
        private final String q;

        private TitleSearch(EntityManager entityManager, Facet[] facets, MultiValueMap<String, String> params, Pageable pageable) {
            this.session = Search.session(entityManager);
            this.facets = facets;
            this.params = params;
            this.pageable = pageable;
            String rawQ = params.getFirst("q");
            this.q = rawQ == null || rawQ.isBlank() ? null : rawQ;
        }

        private Function<SearchPredicateFactory, PredicateFinalStep> predicate(Facet exceptFacet) {
            return f -> f.bool(b -> {
                b.must(f.matchAll());
                if (q != null)
                    b.must(f.simpleQueryString().fields("name", "titleUrl", "seedUrl", "gather.notes").matching(q).defaultOperator(AND));
                for (Facet facet : facets) {
                    facet.search(f, b, params);
                    if (facet == exceptFacet) continue;
                    facet.mustMatch(f, b, params);
                }
            });
        }

        public SearchResults<Title> execute() {
            var search = session.search(Title.class)
                    .where(predicate(null))
                    .sort(f -> q == null ? f.field("name_sort") : f.score());
            // we can do inactive facets as part of the main search
            for (Facet facet : facets) {
                if (!(facet instanceof EntityFacet)) continue;
                if (params.containsKey(facet.param)) continue;
                search.aggregation(((EntityFacet<?>) facet).key, f -> f.terms().field(facet.field, Long.class).maxTermCount(20));
            }

            var result = search.fetch((int) pageable.getOffset(), pageable.getPageSize());

            List<FacetResults> facetResults = new ArrayList<>();
            for (Facet facet : facets) {
                if (facet instanceof EntityFacet && params.containsKey(facet.param)) {
                    // we need to do separate searches for each active entity facets that applies all other facets
                    var facetResult = session.search(Title.class)
                            .where(predicate(facet))
                            .aggregation(((EntityFacet<?>) facet).key, f -> f.terms().field(facet.field, Long.class)
                                    .maxTermCount(20)).fetch(0);
                    facetResults.add(facet.results(params, facetResult));
                } else {
                    facetResults.add(facet.results(params, result));
                }
            }

            return new SearchResults<>(result, facetResults, pageable);
        }

        private SearchScroll<Title> scroll() {
            return session.search(Title.class)
                    .where(predicate(null))
                    .sort(f -> q == null ? f.field("name_sort") : f.score())
                    .scroll(100);
        }
    }
}
