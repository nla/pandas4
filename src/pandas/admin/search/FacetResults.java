package pandas.admin.search;

import java.util.List;

public class FacetResults {
    private final String name;
    private final String param;
    private final List<FacetEntry> entries;
    private final boolean active;
    private final boolean searchable;

    public FacetResults(String name, String param, List<FacetEntry> entries, boolean active, boolean searchable) {
        this.param = param;
        this.name = name;
        this.entries = entries;
        this.active = active;
        this.searchable = searchable;
    }

    public String getName() {
        return name;
    }

    public List<FacetEntry> getEntries() {
        return entries;
    }

    public String getParam() {
        return param;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isVisible() {
        return !entries.isEmpty();
    }

    public String getType() {
        return "entity";
    }

    public boolean isSearchable() {
        return searchable;
    }
}
