package pandas.admin.collection;

import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchResult;
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
import org.springframework.web.util.UriComponentsBuilder;
import pandas.admin.Config;
import pandas.admin.agency.Agency;
import pandas.admin.agency.AgencyRepository;
import pandas.admin.gather.GatherMethod;
import pandas.admin.gather.GatherMethodRepository;
import pandas.admin.gather.GatherSchedule;
import pandas.admin.gather.GatherScheduleRepository;
import pandas.admin.search.FacetEntry;
import pandas.admin.search.FacetResults;
import pandas.admin.search.SearchResults;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.*;
import static org.hibernate.search.engine.search.common.BooleanOperator.AND;
import static pandas.admin.search.SearchUtils.mustMatchAny;

@Controller
public class TitleController {
    private final TitleRepository titleRepository;
    private final Config config;
    private final EntityManager entityManager;
    private final Facet<?>[] facets;

    public TitleController(TitleRepository titleRepository, SubjectRepository subjectRepository, AgencyRepository agencyRepository, StatusRepository statusRepository, GatherMethodRepository gatherMethodRepository, GatherScheduleRepository gatherScheduleRepository, Config config, EntityManager entityManager) {
        this.titleRepository = titleRepository;
        this.config = config;
        this.entityManager = entityManager;
        facets = new Facet<?>[]{
                new Facet<>("Agency", "agency", "agency.id", agencyRepository::findAllById, Agency::getId, Agency::getName),
                new Facet<>("Gather Method", "method", "gather.method.id", gatherMethodRepository::findAllById, GatherMethod::getId, GatherMethod::getName),
                new Facet<>("Gather Schedule", "schedule", "gather.schedule.id", gatherScheduleRepository::findAllById, GatherSchedule::getId, GatherSchedule::getName),
                new Facet<>("Status", "status", "status.id", statusRepository::findAllById, Status::getId, Status::getName),
                new Facet<>("Subject", "subject", "subjects.id", subjectRepository::findAllById, Subject::getId, Subject::getName)
        };
    }

    @GetMapping("/titles/{id}")
    public RedirectView get(@PathVariable("id") long id) {
        return new RedirectView(config.managementDirectActionUrl("titleView?id=" + id));
    }

    private static class Facet<T> {
        private final String name;
        private final String queryParam;
        private final String indexField;
        private final AggregationKey<Map<Long, Long>> key;
        private final Function<Iterable<Long>, Iterable<T>> lookupFunction;
        private final Function<T, Long> idFunction;
        private final Function<T, String> nameFunction;

        private Facet(String name, String queryParam, String indexField,
                      Function<Iterable<Long>, Iterable<T>> lookupFunction,
                      Function<T, Long> idFunction,
                      Function<T, String> nameFunction) {
            this.name = name;
            this.queryParam = queryParam;
            this.indexField = indexField;
            this.key = AggregationKey.of(name);
            this.lookupFunction = lookupFunction;
            this.idFunction = idFunction;
            this.nameFunction = nameFunction;
        }

        Set<Long> parseParam(MultiValueMap<String, String> queryParams) {
            List<String> values = queryParams.get(queryParam);
            if (values == null || values.isEmpty()) return Collections.emptySet();
            Set<Long> ids = new HashSet<>();
            for (String value : values) {
                ids.add(Long.parseLong(value));
            }
            return ids;
        }

        void mustMatch(SearchPredicateFactory predicateFactory, BooleanPredicateClausesStep<?> bool, MultiValueMap<String, String> queryParams) {
            mustMatchAny(predicateFactory, bool, indexField, parseParam(queryParams));
        }

        FacetResults results(MultiValueMap<String, String> queryParams, SearchResult<?> result) {
            var counts = result.aggregation(key);
            Set<Long> activeIds = parseParam(queryParams);
            Set<Long> idSet = new HashSet<>();
            idSet.addAll(counts.keySet());
            idSet.addAll(activeIds);
            List<FacetEntry> entries = new ArrayList<>();
            for (T entity : lookupFunction.apply(idSet)) {
                Long id = idFunction.apply(entity);
                entries.add(new FacetEntry(id, nameFunction.apply(entity), counts.get(id), activeIds.contains(id)));
            }
            entries.sort(comparing(FacetEntry::isActive).thenComparing(FacetEntry::getCount, nullsFirst(naturalOrder())).reversed());
            return new FacetResults(queryParam, name, entries);
        }
    }

    @GetMapping("/titles")
    public String search(@RequestParam(name = "q", required = false) String rawQ,
                         @RequestParam(name = "collection", defaultValue = "") List<Long> collectionIds,
                         @RequestParam MultiValueMap<String, String> queryParams,
                         @PageableDefault(40) Pageable pageable,
                         Model model) {
        String q = (rawQ == null || rawQ.isBlank()) ? null : rawQ;
        SearchSession session = Search.session(entityManager);

        var search = session.search(Title.class)
                .where(f -> f.bool(b -> {
                    b.must(f.matchAll());
                    if (q != null) b.must(f.simpleQueryString().fields("name", "titleUrl", "seedUrl", "gather.notes").matching(q).defaultOperator(AND));
                    mustMatchAny(f, b, "collections.id", collectionIds);
                    for (Facet<?> filter: facets) {
                        filter.mustMatch(f, b, queryParams);
                    }
                }))
                .sort(f -> q == null ? f.field("name_sort") : f.score());

        var uri = UriComponentsBuilder.fromPath("/titles");
        if (q != null) uri.queryParam("q", q);
        if (!collectionIds.isEmpty()) uri.queryParam("collection", collectionIds);
        for (Facet<?> facet : facets) {
            var values = queryParams.get(facet.queryParam);
            if (values != null && !values.isEmpty()) uri.queryParam(facet.queryParam, values);
        }

        List<FacetResults> facetResults = new ArrayList<>();

        for (Facet<?> facet : facets) {
            var facetResult = session.search(Title.class)
                    .where(f -> f.bool(b -> {
                        b.must(f.matchAll());
                        if (q != null)
                            b.must(f.simpleQueryString().fields("name", "titleUrl", "seedUrl").matching(q).defaultOperator(AND));
                        // we apply all filters except the current one
                        for (Facet<?> facet2 : facets) {
                            if (facet != facet2) {
                                facet2.mustMatch(f, b, queryParams);
                            }
                        }
                    })).aggregation(facet.key, f -> f.terms().field(facet.indexField, Long.class)
                            .maxTermCount(20)).fetch(0);
            facetResults.add(facet.results(queryParams, facetResult));
        }

        SearchResults<Title> results = SearchResults.from(search, uri, pageable);
        model.addAttribute("results", results);
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
