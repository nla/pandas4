package pandas.admin.search;

import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.query.SearchResult;

import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.*;

public class Facet {
    private final String queryParam;
    private final String name;
    private final List<FacetEntry> entries;

    public Facet(String queryParam, String name, List<FacetEntry> entries) {
        this.queryParam = queryParam;
        this.name = name;
        this.entries = entries;
    }

    public String getName() {
        return name;
    }

    public List<FacetEntry> getEntries() {
        return entries;
    }

    public String getQueryParam() {
        return queryParam;
    }

    public static <T> Facet from(SearchResult<?> result, AggregationKey<Map<Long, Long>> key, Function<Iterable<Long>, Iterable<T>> lookupFunction,
                                 String queryParam, Function<T, Long> idFunction, Function<T, String> nameFunction, Set<Long> activeIds) {
        var counts = result.aggregation(key);
        Set<Long> idSet = new HashSet<>();
        idSet.addAll(counts.keySet());
        idSet.addAll(activeIds);
        List<FacetEntry> entries = new ArrayList<>();
        for (T entity : lookupFunction.apply(idSet)) {
            Long id = idFunction.apply(entity);
            entries.add(new FacetEntry(id, nameFunction.apply(entity),
                    counts.get(id), activeIds.contains(id)));
        }
        entries.sort(comparing(FacetEntry::isActive).thenComparing(FacetEntry::getCount, nullsFirst(naturalOrder())).reversed());
        return new Facet(queryParam, key.name(), entries);
    }
}
