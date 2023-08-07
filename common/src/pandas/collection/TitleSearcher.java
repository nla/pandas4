package pandas.collection;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchScroll;
import org.hibernate.search.engine.search.sort.dsl.SearchSortFactory;
import org.hibernate.search.engine.search.sort.dsl.SortFinalStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.agency.User;
import pandas.agency.UserRepository;
import pandas.core.Utils;
import pandas.gather.*;
import pandas.search.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.hibernate.search.engine.search.common.BooleanOperator.AND;

@Service
@ConditionalOnProperty(name = "spring.jpa.properties.hibernate.search.enabled", havingValue = "true", matchIfMissing = true)
public class TitleSearcher {
    private static final Logger log = LoggerFactory.getLogger(TitleSearcher.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final Facet[] facets;
    private final Map<String, Function<SearchSortFactory, SortFinalStep>> orderings;

    public TitleSearcher(AgencyRepository agencyRepository,
                         CollectionRepository collectionRepository,
                         FormatRepository formatRepository,
                         GatherMethodRepository gatherMethodRepository,
                         GatherScheduleRepository gatherScheduleRepository,
                         ProfileRepository profileRepository,
                         PublisherRepository publisherRepository,
                         PublisherTypeRepository publisherTypeRepository,
                         ScopeRepository scopeRepository,
                         StatusRepository statusRepository,
                         SubjectRepository subjectRepository,
                         UserRepository userRepository) {
        this.facets = new Facet[]{
                new EntityFacet<>("Agency", "agency", "agency.id", agencyRepository::findAllById, Agency::getId, Agency::getName),
                new DateFacet("Archived", "archived", "archivedDates"),
                new EntityFacet<>("Collection", "collection", "collectionAncestry.id", collectionRepository::findAllById, Collection::getId, Collection::getFullName, List.of("collections.fullName")),
                new DateFacet("Date Registered", "regdate", "regDate"),
                new EntityFacet<>("Format", "format", "format.id", formatRepository::findAllById, Format::getId, Format::getName),
                new EntityFacet<>("Gather Method", "method", "gather.method.id", gatherMethodRepository::findAllById, GatherMethod::getId, GatherMethod::getName),
                new EntityFacet<>("Gather Profile", "profile", "gather.activeProfile.id", profileRepository::findAllById, Profile::getId, Profile::getName),
                new EntityFacet<>("Gather Schedule", "schedule", "gather.schedule.id", gatherScheduleRepository::findAllById, GatherSchedule::getId, GatherSchedule::getName),
                new EntityFacet<>("Gather Scope", "scope", "gather.scope.id", scopeRepository::findAllById, Scope::getId, Scope::getName),
                new DateFacet("First Gather Date", "firstgather", "gather.firstGatherDate"),
                new DateFacet("Last Gather Date", "lastgather", "gather.lastGatherDate"),
                new DateFacet("Next Gather Date", "nextgather", "gather.nextGatherDate"),
                new EntityFacet<>("Owner", "owner", "owner.id", userRepository::findAllById, User::getId, User::getName, List.of("owner.nameGiven", "owner.nameFamily", "owner.userid")),
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

    public List<Title> urlCheck(String url) {
        return Search.session(entityManager).search(Title.class)
                .where(f -> f.phrase().fields("titleUrl", "seedUrl")
                .matching(url)).fetch(5).hits();
    }

    public List<Title> nameCheck(String name) {
        return Search.session(entityManager).search(Title.class)
                .where(f -> f.phrase().fields("name")
                        .matching(name)).fetch(5).hits();
    }

    public List<Title> basicSearch(String q, Long notTitleId) {
        return Search.session(entityManager).search(Title.class)
                .where((f, root) -> {
                    var simpleQuery = f.simpleQueryString().fields("name", "titleUrl", "seedUrl").matching(q).defaultOperator(AND);
                    if (Utils.isNumeric(q)) {
                        var longValue = Long.parseLong(q);
                        var piQuery = f.match().field("pi").matching(longValue);
                        root.add(f.or(simpleQuery, piQuery));
                    } else {
                        root.add(simpleQuery);
                    }
                    if (notTitleId != null) {
                        root.add(f.not(f.id().matching(notTitleId)));
                    }
                })
                .fetch(20).hits();
    }

    private class Query {
        private final SearchSession session;
        private final MultiValueMap<String, String> params;
        private final Pageable pageable;
        private final String q;
        private final String url;
        private boolean not;
        private boolean disappeared;
        private boolean unableToArchive;

        private Query(MultiValueMap<String, String> params, Pageable pageable) {
            this.session = Search.session(entityManager);
            this.params = params;
            this.pageable = pageable;
            String rawQ = params.getFirst("q");
            not = params.containsKey("not");
            disappeared = params.containsKey("disappeared");
            unableToArchive = params.containsKey("unableToArchive");

            StringBuilder qTerms = new StringBuilder();
            StringBuilder urlTerms = new StringBuilder();
            if (rawQ != null) {
                for (String term : rawQ.split(" ")) {
                    if (term.startsWith("http://") || term.startsWith("https://")) {
                        urlTerms.append(term).append(' ');
                    } else {
                        qTerms.append(term).append(' ');
                    }
                }
                this.q = qTerms.toString().strip();
                this.url = urlTerms.toString().strip();
            } else {
                this.q = null;
                this.url = null;
            }
        }

        private PredicateFinalStep predicate(SearchPredicateFactory f, Facet exceptFacet) {
            var and = f.and();
            if (q != null && !q.isBlank()) {
                var simpleQuery = f.simpleQueryString().fields("name", "titleUrl", "seedUrl", "gather.notes").matching(q).defaultOperator(AND);
                if (Utils.isNumeric(q)) {
                    try {
                        long longValue = Long.parseLong(q);
                        var piQuery = f.match().field("pi").matching(longValue);
                        and.add(f.or(piQuery, simpleQuery));
                    } catch (NumberFormatException e) {
                        and.add(simpleQuery);
                    }
                } else {
                    and.add(simpleQuery);
                }
            }
            if (url != null && !url.isBlank()) {
                and.add(f.phrase().fields("titleUrl", "seedUrl").matching(url));
            }
            if (disappeared) and.add(f.match().field("disappeared").matching(true));
            if (unableToArchive) and.add(f.match().field("unableToArchive").matching(true));
            for (Facet facet : facets) {
                and.add(facet.searchPredicate(f, params));
                if (facet == exceptFacet) continue;
                and.add(facet.predicate(f, params, not));
            }
            return and.hasClause() ? and : f.matchAll();
        }

        public SearchResults<Title> execute() {
            var search = session.search(Title.class)
                    .where(f -> predicate(f, null))
                    .sort(this::sort);
            // we can do inactive facets as part of the main search
            for (Facet facet : facets) {
                if (!(facet instanceof EntityFacet)) continue;
                if (params.containsKey(facet.param)) continue;
                search.aggregation(((EntityFacet<?>) facet).key, f -> f.terms().field(facet.field, Long.class).maxTermCount(20));
            }
            var searchQuery = search.toQuery();
            log.info("Query: {}", searchQuery.queryString());
            var result = searchQuery.fetch((int) pageable.getOffset(), pageable.getPageSize());

            List<FacetResults> facetResults = new ArrayList<>();
            for (Facet facet : facets) {
                if (facet instanceof EntityFacet && params.containsKey(facet.param)) {
                    // we need to do separate searches for each active entity facets that applies all other facets
                    var facetResult = session.search(Title.class)
                            .where(f -> predicate(f, facet))
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
                    .where(f -> predicate(f, null))
                    .sort(this::sort)
                    .scroll(100);
        }

        private SortFinalStep sort(SearchSortFactory f) {
            String order = params.getFirst("sort");
            if (order == null || order.isBlank()) order = "Relevance";
            if (order.equals("Relevance") && (q == null || q.isBlank())) order = "Newest";
            return orderings.getOrDefault(order, f2 -> f2.score()).apply(f);
        }
    }
}
