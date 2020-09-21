package pandas.admin.search;

import java.util.List;

public class FacetResults {
    private final String name;
    private final String param;
    private final List<FacetEntry> entries;
    private final boolean active;

    public FacetResults(String name, String param, List<FacetEntry> entries, boolean active) {
        this.param = param;
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
}
