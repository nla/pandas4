package pandas.gather;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateOptionsCollector;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.mapper.orm.Search;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import pandas.collection.*;
import pandas.search.EntityFacet;
import pandas.search.Facet;
import pandas.search.FacetResults;
import pandas.search.SearchResults;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Service
@ConditionalOnProperty(name = "spring.jpa.properties.hibernate.search.enabled", havingValue = "true", matchIfMissing = true)
public class InstanceSearcher {
    private final Facet[] facets;
    @PersistenceContext
    EntityManager entityManager;

    public InstanceSearcher(CollectionRepository collectionRepository, SubjectRepository subjectRepository) {
        this.facets = new Facet[]{
                new EntityFacet<>("Collection", "collection", "title.collectionAncestry.id",
                        collectionRepository::findAllById, Collection::getId, Collection::getFullName),
                new EntityFacet<>("Gather Problem", "problem", "problemIds",
                        GatherProblem::findAllById, GatherProblem::id, GatherProblem::text),
                new EntityFacet<>("Subject", "subject", "title.subjects.id",
                        subjectRepository::findAllById, Subject::getId, Subject::getName),
        };
    }

    public SearchResults<Instance> search(long stateId, Long agencyId, Long ownerId,
                                          MultiValueMap<String, String> params, Pageable pageable) {
        var session = Search.session(entityManager);
        var search = session.search(Instance.class)
                .where(buildPredicate(stateId, agencyId, ownerId, params, null));

        // we can do inactive facets as part of the main search
        for (Facet facet : facets) {
            if (!(facet instanceof EntityFacet)) continue;
            if (params.containsKey(facet.param)) continue;
            search.aggregation(((EntityFacet<?>) facet).key, f -> f.terms().field(facet.field, Long.class).maxTermCount(20));
        }

        var searchQuery = search.toQuery();
        System.out.println(searchQuery.queryString());
        var result = searchQuery.fetch((int) pageable.getOffset(), pageable.getPageSize());
        System.out.println("total " + result.total());

        List<FacetResults> facetResults = new ArrayList<>();
        for (Facet facet : facets) {
            if (facet instanceof EntityFacet && params.containsKey(facet.param)) {
                // we need to do separate searches for each active entity facets that applies all other facets
                var facetResult = session.search(Instance.class)
                        .where(buildPredicate(stateId, agencyId, ownerId, params, facet))
                        .aggregation(((EntityFacet<?>) facet).key, f -> f.terms().field(facet.field, Long.class)
                                .maxTermCount(20)).fetch(0);
                facetResults.add(facet.results(params, facetResult));
            } else {
                facetResults.add(facet.results(params, result));
            }
        }

        return new SearchResults<>(result, facetResults, pageable);
    }

    private BiConsumer<SearchPredicateFactory, BooleanPredicateOptionsCollector<?>> buildPredicate(
            long stateId, Long agencyId, Long ownerId, MultiValueMap<String, String> params, Facet excludedFacet) {
        return (f, b) ->
        {
            b.must(Facet.matchAllExcept(f, params, facets, excludedFacet));
            b.must(f.match().field("state.id").matching(stateId));
            if (agencyId != null) b.must(f.match().field("title.agency.id").matching(agencyId));
            if (ownerId != null) b.must(f.match().field("title.owner.id").matching(ownerId));
        };
    }
}

