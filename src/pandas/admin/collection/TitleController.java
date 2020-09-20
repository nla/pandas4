package pandas.admin.collection;

import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import pandas.admin.search.Facet;
import pandas.admin.search.FacetResults;
import pandas.admin.search.SearchResults;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;
import static org.hibernate.search.engine.search.common.BooleanOperator.AND;

@Controller
public class TitleController {
    private final TitleRepository titleRepository;
    private final Config config;
    private final EntityManager entityManager;
    private final Facet<?>[] facets;

    public TitleController(TitleRepository titleRepository, SubjectRepository subjectRepository, AgencyRepository agencyRepository, CollectionRepository collectionRepository, FormatRepository formatRepository, StatusRepository statusRepository, GatherMethodRepository gatherMethodRepository, GatherScheduleRepository gatherScheduleRepository, IndividualRepository individualRepository, PublisherRepository publisherRepository, PublisherTypeRepository publisherTypeRepository, Config config, EntityManager entityManager) {
        this.titleRepository = titleRepository;
        this.config = config;
        this.entityManager = entityManager;
        facets = new Facet<?>[]{
                new Facet<>("Agency", "agency", "agency.id", agencyRepository::findAllById, Agency::getId, Agency::getName),
                new Facet<>("Collection", "collection", "collections.id", collectionRepository::findAllById, Collection::getId, Collection::getFullName),
                new Facet<>("Format", "format", "format.id", formatRepository::findAllById, Format::getId, Format::getName),
                new Facet<>("Gather Method", "method", "gather.method.id", gatherMethodRepository::findAllById, GatherMethod::getId, GatherMethod::getName),
                new Facet<>("Gather Schedule", "schedule", "gather.schedule.id", gatherScheduleRepository::findAllById, GatherSchedule::getId, GatherSchedule::getName),
                new Facet<>("Owner", "owner", "owner.id", individualRepository::findAllById, Individual::getId, Individual::getName),
                new Facet<>("Publisher", "publisher", "publisher.id", publisherRepository::findAllById, Publisher::getId, Publisher::getName),
                new Facet<>("Publisher Type", "publisher.type", "publisher.type.id", publisherTypeRepository::findAllById, PublisherType::getId, PublisherType::getName),
                new Facet<>("Status", "status", "status.id", statusRepository::findAllById, Status::getId, Status::getName),
                new Facet<>("Subject", "subject", "subjects.id", subjectRepository::findAllById, Subject::getId, Subject::getName),
                new Facet<>("Subject 2", "subject2", "subjects.id", subjectRepository::findAllById, Subject::getId, Subject::getName)
        };
    }

    @GetMapping("/titles/{id}")
    public RedirectView get(@PathVariable("id") long id) {
        return new RedirectView(config.managementDirectActionUrl("titleView?id=" + id));
    }

    @GetMapping("/titles")
    public String search(@RequestParam(name = "q", required = false) String rawQ,
                         @RequestParam MultiValueMap<String, String> queryParams,
                         @PageableDefault(20) Pageable pageable,
                         Model model) {
        String q = (rawQ == null || rawQ.isBlank()) ? null : rawQ;
        SearchSession session = Search.session(entityManager);
        List<FacetResults> facetResults = new ArrayList<>();

        var search = session.search(Title.class)
                .where(f -> f.bool(b -> {
                    b.must(f.matchAll());
                    if (q != null)
                        b.must(f.simpleQueryString().fields("name", "titleUrl", "seedUrl", "gather.notes").matching(q).defaultOperator(AND));
                    for (Facet<?> filter : facets) {
                        filter.mustMatch(f, b, queryParams);
                    }
                }))
                .sort(f -> q == null ? f.field("name_sort") : f.score());

        // we can do inactive facets as part of the main search
        for (Facet<?> facet : facets) {
            if (!queryParams.containsKey(facet.queryParam)) {
                search.aggregation(facet.key, f -> f.terms().field(facet.indexField, Long.class).maxTermCount(20));
            }
        }

        var result = search.fetch((int) pageable.getOffset(), pageable.getPageSize());
        for (Facet<?> facet : facets) {
            if (!queryParams.containsKey(facet.queryParam)) {
                facetResults.add(facet.results(queryParams, result));
            }
        }

        // we need to do separate searches for each active facet that applies all other facets
        for (Facet<?> facet : facets) {
            if (!queryParams.containsKey(facet.queryParam)) continue;
            var facetResult = session.search(Title.class)
                    .where(f -> f.bool(b -> {
                        b.must(f.matchAll());
                        if (q != null)
                            b.must(f.simpleQueryString().fields("name", "titleUrl", "seedUrl").matching(q).defaultOperator(AND));
                        for (Facet<?> facet2 : facets) {
                            if (facet != facet2) {
                                facet2.mustMatch(f, b, queryParams);
                            }
                        }
                    })).aggregation(facet.key, f -> f.terms().field(facet.indexField, Long.class)
                            .maxTermCount(20)).fetch(0);
            facetResults.add(facet.results(queryParams, facetResult));
        }

        facetResults.sort(comparing(FacetResults::getName));

        model.addAttribute("results", new SearchResults<>(result, pageable));
        model.addAttribute("q", q);
        model.addAttribute("facets", facetResults);
        return "TitleSearch";
    }

    @GetMapping("/titles/reindex")
    @ResponseBody
    public String reindex() throws InterruptedException {
        Search.session(entityManager).massIndexer(Title.class).startAndWait();
        return "ok";
    }
}
