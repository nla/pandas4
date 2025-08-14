package pandas.search;

import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;
import org.springframework.util.MultiValueMap;
import pandas.collection.Title;

import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.*;
import static pandas.search.SearchUtils.mustMatchAny;

public class EnumFacet<T extends Enum<T>> extends Facet {
    public final AggregationKey<Map<T, Long>> key;
    private final Function<T, String> nameFunction;
    private final String[] searchFields;
    public final Class<T> type;

    public EnumFacet(Class<T> type, String name, String param, String field,
                     Function<T, String> nameFunction) {
        this(type, name, param, field, nameFunction, List.of());
    }

    public EnumFacet(Class<T> type, String name, String param, String field,
                     Function<T, String> nameFunction, List<String> searchFields) {
        super(name, param, field);
        this.type = type;
        this.key = AggregationKey.of(name);
        this.nameFunction = nameFunction;
        this.searchFields = searchFields.toArray(new String[0]);
    }

    private Set<T> parseParam(MultiValueMap<String, String> queryParams) {
        List<String> values = queryParams.get(param);
        if (values == null || values.isEmpty()) return Collections.emptySet();
        Set<T> ids = new HashSet<>();
        for (String value : values) {
            ids.add(Enum.valueOf(type, value));
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
            String search = queryParams.getFirst(param);
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
        Set<T> activeIds = parseParam(queryParams);
        Set<T> idSet = new HashSet<>();
        idSet.addAll(counts.keySet());
        idSet.addAll(activeIds);
        for (T entity : idSet) {
            entries.add(new FacetEntry(entity.toString(), nameFunction.apply(entity), counts.get(entity), activeIds.contains(entity)));
        }
        entries.sort(comparing(FacetEntry::isActive)
                .thenComparing(FacetEntry::getCount, nullsFirst(naturalOrder()))
                .reversed()
                .thenComparing(FacetEntry::getName));
        String search = queryParams.getFirst(param);
        if (search != null && search.isBlank()) search = null;
        boolean active = !activeIds.isEmpty() || search != null;
        return new FacetResults(name, param, entries, active, searchFields.length != 0, search);
    }

    public void aggregate(SearchQueryOptionsStep<? extends SearchQueryOptionsStep<?, Title, SearchLoadingOptionsStep, ?, ?>, Title, SearchLoadingOptionsStep, ?, ?> search) {
        search.aggregation(key, f -> f.terms().field(field, type).maxTermCount(20));
    }
}
