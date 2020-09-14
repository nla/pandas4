package pandas.admin.search;

import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.query.SearchFetchable;
import org.hibernate.search.engine.search.query.SearchResult;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SearchResults<T> extends PageImpl<T> {
    private final UriComponentsBuilder uriBuilder;
    public final SearchResult<T> raw;

    public SearchResults(SearchResult<T> result, UriComponentsBuilder uriBuilder, Pageable pageable) {
        super(result.hits(), pageable, result.total().hitCount());
        this.uriBuilder = uriBuilder;
        this.raw = result;
    }

    public static <T> SearchResults<T> from(SearchFetchable<T> search, UriComponentsBuilder uriBuilder, Pageable pageable) {
        return new SearchResults<T>(search.fetch((int)pageable.getOffset(), pageable.getPageSize()), uriBuilder, pageable);
    }

    public String nextUrl() {
        return hasNext() ? uriBuilder.cloneBuilder().queryParam("page", nextPageable().getPageNumber()).toUriString() : null;
    }

    public String previousUrl() {
        return hasPrevious() ? uriBuilder.cloneBuilder().queryParam("page", previousPageable().getPageNumber()).toUriString() : null;
    }

    public <T> Facet facet(AggregationKey<Map<Long, Long>> key, Function<Iterable<Long>, Iterable<T>> lookupFunction,
                              String queryParam, Function<T, Long> idFunction, Function<T, String> nameFunction) {
        var counts = raw.aggregation(key);
        var entities = new HashMap<Long,T>();
        for (T entity : lookupFunction.apply(counts.keySet())) {
            entities.put(idFunction.apply(entity), entity);
        }
        List<FacetEntry> entries = new ArrayList<>();
        for (var count: counts.entrySet()) {
            T entity = entities.get(count.getKey());
            entries.add(new FacetEntry(nameFunction.apply(entity),
                    uriBuilder.cloneBuilder().queryParam(queryParam, count.getKey()).toUriString(),
                    count.getValue()));
        }
        return new Facet(key.name(), entries);
    }
}
