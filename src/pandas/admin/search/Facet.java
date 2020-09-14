package pandas.admin.search;

import java.util.List;

public class Facet {
    private final String name;
    private final List<FacetEntry> entries;

    public Facet(String name, List<FacetEntry> entries) {
        this.name = name;
        this.entries = entries;
    }

    public String getName() {
        return name;
    }

    public List<FacetEntry> getEntries() {
        return entries;
    }
}
