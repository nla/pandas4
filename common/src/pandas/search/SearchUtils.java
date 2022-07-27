package pandas.search;

import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.util.Objects;

public class SearchUtils {
    public static final long ID_NONE = -5252203186331998330L;

    public static PredicateFinalStep matchAny(SearchPredicateFactory f, String field, java.util.Collection<?> values) {
        return f.bool(b -> values.forEach(value -> b.should(Objects.equals(value, ID_NONE) ?
                f.bool().mustNot(f.exists().field(field)) : f.match().field(field).matching(value))));
    }

    public static void mustMatchAny(SearchPredicateFactory f, BooleanPredicateClausesStep<?> b, String field, java.util.Collection<?> values) {
        if (values != null && !values.isEmpty()) b.must(matchAny(f, field, values));
    }
}