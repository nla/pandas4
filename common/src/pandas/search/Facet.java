package pandas.search;

import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;
import org.springframework.util.MultiValueMap;
import pandas.gather.Instance;

import java.util.Arrays;

public abstract class Facet {
    public final String param;
    public final String field;
    protected final String name;

    public Facet(String name, String param, String field) {
        this.name = name;
        this.param = param;
        this.field = field;
    }

    public abstract void mustMatch(SearchPredicateFactory predicateFactory, BooleanPredicateClausesStep<?> bool, MultiValueMap<String, String> queryParams);

    public abstract void search(SearchPredicateFactory predicateFactory, BooleanPredicateClausesStep<?> bool, MultiValueMap<String, String> queryParams);

    public abstract FacetResults results(MultiValueMap<String, String> queryParams, SearchResult<?> result);

    public static SearchPredicate matchAll(SearchPredicateFactory f, MultiValueMap<String, String> params, Facet[] facets) {
        var b = f.bool();
        b.must(f.matchAll());
        for (Facet facet : facets) {
            facet.mustMatch(f, b, params);
        }
        return b.toPredicate();
    }

    public static SearchPredicate matchAllExcept(SearchPredicateFactory f, MultiValueMap<String, String> params, Facet[] facets, Facet excluded) {
        return matchAll(f, params, Arrays.stream(facets).filter(facet -> facet.equals(excluded)).toArray(Facet[]::new));
    }
}
