package pandas.collection;

import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchScroll;
import org.hibernate.search.engine.search.sort.dsl.SearchSortFactory;
import org.hibernate.search.engine.search.sort.dsl.SortFinalStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.core.Config;
import pandas.core.Individual;
import pandas.core.IndividualRepository;
import pandas.core.UserService;
import pandas.gather.*;
import pandas.search.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.hibernate.search.engine.search.common.BooleanOperator.AND;

@Service
public class TitleSearcher {
    @PersistenceContext
    private EntityManager entityManager;

    private final Facet[] facets;
    private final Map<String, Function<SearchSortFactory, SortFinalStep>> orderings;

    public TitleSearcher(SubjectRepository subjectRepository, AgencyRepository agencyRepository, CollectionRepository collectionRepository, FormatRepository formatRepository, StatusRepository statusRepository, GatherMethodRepository gatherMethodRepository, GatherScheduleRepository gatherScheduleRepository, IndividualRepository individualRepository, PublisherRepository publisherRepository, PublisherTypeRepository publisherTypeRepository, Config config, EntityManager entityManager, TitleRepository titleRepository, TitleGatherRepository titleGatherRepository, UserService userService, GatherService gatherService, OwnerHistoryRepository ownerHistoryRepository, GatherDateRepository gatherDateRepository) {
        this.facets = new Facet[]{
                new EntityFacet<>("Agency", "agency", "agency.id", agencyRepository::findAllById, Agency::getId, Agency::getName),
                new EntityFacet<>("Collection", "collection", "collections.id", collectionRepository::findAllById, Collection::getId, Collection::getFullName, List.of("collections.fullName")),
                new DateFacet("Date Registered", "regdate", "regDate"),
                new EntityFacet<>("Format", "format", "format.id", formatRepository::findAllById, Format::getId, Format::getName),
                new EntityFacet<>("Gather Method", "method", "gather.method.id", gatherMethodRepository::findAllById, GatherMethod::getId, GatherMethod::getName),
                new EntityFacet<>("Gather Schedule", "schedule", "gather.schedule.id", gatherScheduleRepository::findAllById, GatherSchedule::getId, GatherSchedule::getName),
                new DateFacet("First Gather Date", "firstgather", "gather.firstGatherDate"),
                new DateFacet("Last Gather Date", "lastgather", "gather.lastGatherDate"),
                new DateFacet("Next Gather Date", "nextgather", "gather.nextGatherDate"),
                new EntityFacet<>("Owner", "owner", "owner.id", individualRepository::findAllById, Individual::getId, Individual::getName, List.of("owner.nameGiven", "owner.nameFamily", "owner.userid")),
                new EntityFacet<>("Publisher", "publisher", "publisher.id", publisherRepository::findAllById, Publisher::getId, Publisher::getName, List.of("publisher.organisation.name")),
                new EntityFacet<>("Publisher Type", "publisher.type", "publisher.type.id", publisherTypeRepository::findAllById, PublisherType::getId, PublisherType::getName),
                new EntityFacet<>("Status", "status", "status.id", statusRepository::findAllById, Status::getId, Status::getName),
                new EntityFacet<>("Subject", "subject", "subjects.id", subjectRepository::findAllById, Subject::getId, Subject::getName, List.of("subjects.fullName")),
                new EntityFacet<>("Subject 2", "subject2", "subjects.id", subjectRepository::findAllById, Subject::getId, Subject::getName, List.of("subjects.fullName"))
        };
        orderings = new LinkedHashMap<>();
        orderings.put("Relevance", f -> f.score());
        orderings.put("Newest", f -> f.field("regDate").desc());
        orderings.put("Oldest", f -> f.field("regDate"));
        orderings.put("Recently Gathered", f -> f.field("gather.lastGatherDate").desc());
        orderings.put("Name (ascending)", f -> f.field("name_sort"));
        orderings.put("Name (descending)", f -> f.field("name_sort").desc());
    }
    
    public SearchResults<Title> search(MultiValueMap<String, String> params, Pageable pageable) {
        return new Query(params, pageable).execute();
    }

    public SearchScroll<Title> scroll(MultiValueMap<String, String> params) {
        return new Query(params, Pageable.unpaged()).scroll();
    }

    public Map<String, Function<SearchSortFactory, SortFinalStep>> getOrderings() {
        return orderings;
    }

    public Map<Long, Long> countTitlesBySchedule() {
        AggregationKey<Map<Long, Long>> titlesBySchedule = AggregationKey.of("titlesBySchedule");
        var result = Search.session(entityManager).search(Title.class).
                where(f -> f.matchAll()).aggregation(titlesBySchedule, f -> f.terms().field("gather.schedule.id", Long.class))
                .fetch(0);
        return result.aggregation(titlesBySchedule);
    }

    private class Query {
        private final SearchSession session;
        private final MultiValueMap<String, String> params;
        private final Pageable pageable;
        private final String q;

        private Query(MultiValueMap<String, String> params, Pageable pageable) {
            this.session = Search.session(entityManager);
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
                    .sort(this::sort);
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

        public SearchScroll<Title> scroll() {
            return session.search(Title.class)
                    .where(predicate(null))
                    .sort(this::sort)
                    .scroll(100);
        }

        private SortFinalStep sort(SearchSortFactory f) {
            String order = params.getFirst("sort");
            if (order == null || order.isBlank()) order = "Relevance";
            if (order.equals("Relevance") && q == null) order = "Name (ascending)";
            return orderings.getOrDefault(order, f2 -> f2.score()).apply(f);
        }
    }
}
