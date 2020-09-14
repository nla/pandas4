package pandas.admin.core;

import org.hibernate.search.engine.search.query.SearchFetchable;
import org.hibernate.search.engine.search.query.SearchResult;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.util.UriComponentsBuilder;

public class SearchResults<T> extends PageImpl<T> {
    private final UriComponentsBuilder uriBuilder;

    public SearchResults(SearchResult<T> result, UriComponentsBuilder uriBuilder, Pageable pageable) {
        super(result.hits(), pageable, result.total().hitCount());
        this.uriBuilder = uriBuilder;
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
}
