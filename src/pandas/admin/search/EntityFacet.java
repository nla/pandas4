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

public class EntityFacet<T> extends Facet {
    public final AggregationKey<Map<Long, Long>> key;
    private final Function<Iterable<Long>, Iterable<T>> lookupFunction;
    private final Function<T, Long> idFunction;
    private final Function<T, String> nameFunction;
    private final boolean searchable;

    public EntityFacet(String name, String param, String field,
                       Function<Iterable<Long>, Iterable<T>> lookupFunction,
                       Function<T, Long> idFunction,
                       Function<T, String> nameFunction) {
        this(name, param, field, lookupFunction, idFunction, nameFunction, false);
    }

    public EntityFacet(String name, String param, String field,
                       Function<Iterable<Long>, Iterable<T>> lookupFunction,
                       Function<T, Long> idFunction,
                       Function<T, String> nameFunction, boolean searchable) {
        super(name, param, field);
        this.key = AggregationKey.of(name);
        this.lookupFunction = lookupFunction;
        this.idFunction = idFunction;
        this.nameFunction = nameFunction;
        this.searchable = searchable;
    }

    private Set<Long> parseParam(MultiValueMap<String, String> queryParams) {
        List<String> values = queryParams.get(param);
        if (values == null || values.isEmpty()) return Collections.emptySet();
        Set<Long> ids = new HashSet<>();
        for (String value : values) {
            ids.add(Long.parseLong(value));
        }
        return ids;
    }

    @Override
    public void mustMatch(SearchPredicateFactory predicateFactory, BooleanPredicateClausesStep<?> bool, MultiValueMap<String, String> queryParams) {
        mustMatchAny(predicateFactory, bool, field, parseParam(queryParams));
    }

    @Override
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
        entries.sort(comparing(FacetEntry::isActive)
                .thenComparing(FacetEntry::getCount, nullsFirst(naturalOrder()))
                .reversed()
                .thenComparing(FacetEntry::getName));
        return new FacetResults(name, param, entries, !activeIds.isEmpty(), searchable);
    }
}
