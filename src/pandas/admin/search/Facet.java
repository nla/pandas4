package pandas.admin.search;

import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.*;
import static pandas.admin.search.SearchUtils.mustMatchAny;

public class Facet<T> {
    private final String name;
    public final String queryParam;
    public final String indexField;
    public final AggregationKey<Map<Long, Long>> key;
    private final Function<Iterable<Long>, Iterable<T>> lookupFunction;
    private final Function<T, Long> idFunction;
    private final Function<T, String> nameFunction;

    public Facet(String name, String queryParam, String indexField,
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

    private Set<Long> parseParam(MultiValueMap<String, String> queryParams) {
        List<String> values = queryParams.get(queryParam);
        if (values == null || values.isEmpty()) return Collections.emptySet();
        Set<Long> ids = new HashSet<>();
        for (String value : values) {
            ids.add(Long.parseLong(value));
        }
        return ids;
    }

    public void mustMatch(SearchPredicateFactory predicateFactory, BooleanPredicateClausesStep<?> bool, MultiValueMap<String, String> queryParams) {
        mustMatchAny(predicateFactory, bool, indexField, parseParam(queryParams));
    }

    public FacetResults results(MultiValueMap<String, String> queryParams, SearchResult<?> result) {
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
        return new FacetResults(queryParam, name, entries, !activeIds.isEmpty());
    }
}
