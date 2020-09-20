package pandas.admin.search;

import org.hibernate.search.engine.search.query.SearchFetchable;
import org.hibernate.search.engine.search.query.SearchResult;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class SearchResults<T> extends PageImpl<T> {
    public final SearchResult<T> raw;

    public SearchResults(SearchResult<T> result, Pageable pageable) {
        super(result.hits(), pageable, result.total().hitCount());
        this.raw = result;
    }

    public static <T> SearchResults<T> from(SearchFetchable<T> search, Pageable pageable) {
        return new SearchResults<T>(search.fetch((int) pageable.getOffset(), pageable.getPageSize()), pageable);
    }
}
