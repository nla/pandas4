package pandas.admin.search;

import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.util.List;

public class SearchUtils {
    public static PredicateFinalStep matchAny(SearchPredicateFactory f, String field, java.util.Collection<?> values) {
        return f.bool(b -> values.forEach(value -> b.should(f.match().field(field).matching(value))));
    }

    public static void mustMatchAny(SearchPredicateFactory f, BooleanPredicateClausesStep<?> b, String field, java.util.Collection<?> values) {
        if (values != null && !values.isEmpty()) b.must(matchAny(f, field, values));
    }
}
