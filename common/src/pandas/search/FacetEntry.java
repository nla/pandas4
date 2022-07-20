package pandas.search;

public class FacetEntry {
    private final String id;
    private final String name;
    private final Long count;
    private final boolean active;

    public FacetEntry(String id, String name, Long count, boolean active) {
        this.name = name;
        this.id = id;
        this.count = count;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public Long getCount() {
        return count;
    }

    public boolean isActive() {
        return active;
    }

    public String getId() {
        return id;
    }
}
