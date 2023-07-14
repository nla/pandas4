package pandas.search;

import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateOptionsCollector;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.util.Collection;
import java.util.Objects;

public class SearchUtils {
    public static final long ID_NONE = -5252203186331998330L;

    public static SearchPredicate matchAny(SearchPredicateFactory f, String field, Collection<?> values) {
        var or = f.or();
        for (var value : values) {
            if (Objects.equals(value, ID_NONE)) {
                or.add(f.not(f.exists().field(field)));
            } else {
                or.add(f.match().field(field).matching(value));
            }
        }
        return or.toPredicate();
    }

    public static void mustMatchAny(SearchPredicateFactory f, BooleanPredicateOptionsCollector<?> b, String field, java.util.Collection<?> values) {
        mustMatchAny(f, b, field, values, false);
    }

    public static void mustMatchAny(SearchPredicateFactory f, BooleanPredicateOptionsCollector<?> b, String field, java.util.Collection<?> values, boolean not) {
        if (values != null && !values.isEmpty()) {
            if (not) {
                b.mustNot(matchAny(f, field, values));
            }else {
                b.must(matchAny(f, field, values));
            }
        }
    }

    public static SearchPredicate mustMatchAny(SearchPredicateFactory f, String field, Collection<?> values, boolean not) {
        if (values != null && !values.isEmpty()) {
            if (not) {
                return f.not(matchAny(f, field, values)).toPredicate();
            } else {
                return matchAny(f, field, values);
            }
        }
        return f.matchAll().toPredicate();
    }
}
