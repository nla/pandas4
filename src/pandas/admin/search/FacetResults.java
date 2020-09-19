package pandas.admin.search;

import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.query.SearchResult;

import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.*;

public class FacetResults {
    private final String queryParam;
    private final String name;
    private final List<FacetEntry> entries;

    public FacetResults(String queryParam, String name, List<FacetEntry> entries) {
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
}
