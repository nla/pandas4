package pandas.gather;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.mapper.orm.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import pandas.agency.User;
import pandas.agency.UserRepository;
import pandas.collection.Collection;
import pandas.collection.CollectionRepository;
import pandas.collection.Subject;
import pandas.collection.SubjectRepository;
import pandas.search.EntityFacet;
import pandas.search.Facet;
import pandas.search.FacetResults;
import pandas.search.SearchResults;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "spring.jpa.properties.hibernate.search.enabled", havingValue = "true", matchIfMissing = true)
public class InstanceSearcher {
    private static final Logger log = LoggerFactory.getLogger(InstanceSearcher.class);
    private final Facet[] facets;
    @PersistenceContext
    EntityManager entityManager;

    public InstanceSearcher(CollectionRepository collectionRepository,
                            GatherMethodRepository gatherMethodRepository,
                            GatherScheduleRepository gatherScheduleRepository,
                            SubjectRepository subjectRepository,
                            UserRepository userRepository) {
        this.facets = new Facet[]{
                new EntityFacet<>("Collection", "collection", "title.collectionAncestry.id",
                        collectionRepository::findAllById, Collection::getId, Collection::getFullName),
                new EntityFacet<>("Gather Method", "method", "title.gather.method.id",
                        gatherMethodRepository::findAllById, GatherMethod::getId, GatherMethod::getName),
                new EntityFacet<>("Gather Schedule", "schedule", "title.gather.schedule.id",
                        gatherScheduleRepository::findAllById, GatherSchedule::getId, GatherSchedule::getName),
                new EntityFacet<>("Gather Problem", "problem", "problemIds",
                        GatherProblem::findAllById, GatherProblem::id, GatherProblem::text),
                new EntityFacet<>("Owner", "owner", "title.owner.id",
                        userRepository::findAllById, User::getId, User::getName),
                new EntityFacet<>("Subject", "subject", "title.subjects.id",
                        subjectRepository::findAllById, Subject::getId, Subject::getName),
        };
    }

    public SearchResults<Instance> search(long stateId, Long agencyId, Long ownerId,
                                          MultiValueMap<String, String> params, Pageable pageable) {
        var session = Search.session(entityManager);
        var scope = session.scope(Instance.class);
        var search = session.search(scope)
                .where(buildPredicate(scope.predicate(), stateId, agencyId, ownerId, params, null))
                .sort(f -> f.field("date").desc());

        // we can do inactive facets as part of the main search
        for (Facet facet : facets) {
            if (!(facet instanceof EntityFacet)) continue;
            if (params.containsKey(facet.param)) continue;
            search.aggregation(((EntityFacet<?>) facet).key, f -> f.terms().field(facet.field, Long.class).maxTermCount(20));
        }

        var searchQuery = search.toQuery();
        var result = searchQuery.fetch((int) pageable.getOffset(), pageable.getPageSize());
        log.info("Instance search (hits={} time={}ms}: {}", result.total(), result.took().toMillis(),
                searchQuery.queryString());

        List<FacetResults> facetResults = new ArrayList<>();
        for (Facet facet : facets) {
            if (facet instanceof EntityFacet && params.containsKey(facet.param)) {
                // we need to do separate searches for each active entity facets that applies all other facets
                var facetResult = session.search(scope)
                        .where(buildPredicate(scope.predicate(), stateId, agencyId, ownerId, params, facet))
                        .aggregation(((EntityFacet<?>) facet).key, f -> f.terms().field(facet.field, Long.class)
                                .maxTermCount(20)).fetch(0);
                facetResults.add(facet.results(params, facetResult));
            } else {
                facetResults.add(facet.results(params, result));
            }
        }

        return new SearchResults<>(result, facetResults, pageable);
    }

    private SearchPredicate buildPredicate(SearchPredicateFactory f,
            long stateId, Long agencyId, Long ownerId, MultiValueMap<String, String> params, Facet excludedFacet) {
        var and = f.and();
        for (Facet facet : facets) {
            if (facet == excludedFacet) continue;
            and.add(facet.predicate(f, params, false));
        }
        and.add(f.match().field("state.id").matching(stateId));
        if (agencyId != null) and.add(f.match().field("title.agency.id").matching(agencyId));
        if (ownerId != null) and.add(f.match().field("title.owner.id").matching(ownerId));
        return and.toPredicate();
    }
}

