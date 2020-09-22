package pandas.admin.search;

import org.hibernate.search.engine.search.query.SearchResult;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class SearchResults<T> extends PageImpl<T> {
    public final SearchResult<T> raw;
    private final List<FacetResults> facets;

    public SearchResults(SearchResult<T> result, List<FacetResults> facets, Pageable pageable) {
        super(result.hits(), pageable, result.total().hitCount());
        this.raw = result;
        this.facets = facets;
    }

    public List<FacetResults> getFacets() {
        return facets;
    }
}
