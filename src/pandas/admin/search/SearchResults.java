package pandas.admin.search;

import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.query.SearchFetchable;
import org.hibernate.search.engine.search.query.SearchResult;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
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

    public <I, T> Facet facet(AggregationKey<Map<I, Long>> key, Function<Iterable<I>, Iterable<T>> lookupFunction,
                              String queryParam, Function<T, Long> idFunction, Function<T, String> nameFunction) {
        var aggr = raw.aggregation(key);
        Iterator<T> ki = lookupFunction.apply(aggr.keySet()).iterator();
        Iterator<Long> vi = ((Iterable<Long>) aggr.values()).iterator();
        List<FacetEntry> entries = new ArrayList<>();
        while (ki.hasNext() && vi.hasNext()) {
            T value = ki.next();
            entries.add(new FacetEntry(nameFunction.apply(value),
                    uriBuilder.cloneBuilder().queryParam(queryParam, idFunction.apply(value)).toUriString(),
                    vi.next()));
        }
        return new Facet(key.name(), entries);
    }
}
