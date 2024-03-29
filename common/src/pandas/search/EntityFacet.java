package pandas.search;

import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.*;
import static pandas.search.SearchUtils.ID_NONE;
import static pandas.search.SearchUtils.mustMatchAny;

public class EntityFacet<T> extends Facet {
    public final AggregationKey<Map<Long, Long>> key;
    private final Function<Iterable<Long>, Iterable<T>> lookupFunction;
    private final Function<T, Long> idFunction;
    private final Function<T, String> nameFunction;
    private final String[] searchFields;

    public EntityFacet(String name, String param, String field,
                       Function<Iterable<Long>, Iterable<T>> lookupFunction,
                       Function<T, Long> idFunction,
                       Function<T, String> nameFunction) {
        this(name, param, field, lookupFunction, idFunction, nameFunction, List.of());
    }

    public EntityFacet(String name, String param, String field,
                       Function<Iterable<Long>, Iterable<T>> lookupFunction,
                       Function<T, Long> idFunction,
                       Function<T, String> nameFunction, List<String> searchFields) {
        super(name, param, field);
        this.key = AggregationKey.of(name);
        this.lookupFunction = lookupFunction;
        this.idFunction = idFunction;
        this.nameFunction = nameFunction;
        this.searchFields = searchFields.toArray(new String[0]);
    }

    private Set<Long> parseParam(MultiValueMap<String, String> queryParams) {
        List<String> values = queryParams.get(param);
        if (values == null || values.isEmpty()) return Collections.emptySet();
        Set<Long> ids = new HashSet<>();
        for (String value : values) {
            if (value.equals("none")) {
                ids.add(SearchUtils.ID_NONE);
            } else {
                ids.add(Long.parseLong(value));
            }
        }
        return ids;
    }

    @Override
    public SearchPredicate predicate(SearchPredicateFactory predicateFactory, MultiValueMap<String, String> queryParams,
                                     boolean not) {
        return mustMatchAny(predicateFactory, field, parseParam(queryParams), not);
    }

    @Override
    public SearchPredicate searchPredicate(SearchPredicateFactory f, MultiValueMap<String, String> queryParams) {
        if (searchFields.length != 0) {
            String search = queryParams.getFirst(param + ".name");
            if (search != null && !search.isBlank()) {
                return f.simpleQueryString().fields(searchFields).matching(search).toPredicate();
            }
        }
        return f.matchAll().toPredicate();
    }

    @Override
    public FacetResults results(MultiValueMap<String, String> queryParams, SearchResult<?> result) {
        var counts = result.aggregation(key);
        List<FacetEntry> entries = new ArrayList<>();
        Set<Long> activeIds = parseParam(queryParams);
        Set<Long> idSet = new HashSet<>();
        idSet.addAll(counts.keySet());
        idSet.addAll(activeIds);
        if (activeIds.contains(ID_NONE)) {
            entries.add(new FacetEntry("none", "No " + name.toLowerCase() + "s", null, true));
        }
        for (T entity : lookupFunction.apply(idSet)) {
            Long id = idFunction.apply(entity);
            entries.add(new FacetEntry(id.toString(), nameFunction.apply(entity), counts.get(id), activeIds.contains(id)));
        }
        entries.sort(comparing(FacetEntry::isActive)
                .thenComparing(FacetEntry::getCount, nullsFirst(naturalOrder()))
                .reversed()
                .thenComparing(FacetEntry::getName));
        String search = queryParams.getFirst(param + ".name");
        if (search != null && search.isBlank()) search = null;
        boolean active = !activeIds.isEmpty() || search != null;
        return new FacetResults(name, param, entries, active, searchFields.length != 0, search);
    }
}
