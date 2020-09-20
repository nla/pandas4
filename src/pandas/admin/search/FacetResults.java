package pandas.admin.search;

import java.util.List;

public class FacetResults {
    private final String queryParam;
    private final String name;
    private final List<FacetEntry> entries;
    private final boolean active;

    public FacetResults(String queryParam, String name, List<FacetEntry> entries, boolean active) {
        this.queryParam = queryParam;
        this.name = name;
        this.entries = entries;
        this.active = active;
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

    public boolean isActive() {
        return active;
    }
}
