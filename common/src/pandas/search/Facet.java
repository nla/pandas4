package pandas.search;

import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.springframework.util.MultiValueMap;

public abstract class Facet {
    public final String param;
    public final String field;
    protected final String name;

    public Facet(String name, String param, String field) {
        this.name = name;
        this.param = param;
        this.field = field;
    }

    public abstract SearchPredicate predicate(SearchPredicateFactory predicateFactory, MultiValueMap<String, String> queryParams,
                                              boolean not);

    public abstract SearchPredicate searchPredicate(SearchPredicateFactory predicateFactory, MultiValueMap<String, String> queryParams);

    public abstract FacetResults results(MultiValueMap<String, String> queryParams, SearchResult<?> result);

}
