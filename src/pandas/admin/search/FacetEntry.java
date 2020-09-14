package pandas.admin.search;

public class FacetEntry {
    private final long id;
    private final String name;
    private final Long count;
    private final boolean active;

    public FacetEntry(long id, String name, Long count, boolean active) {
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

    public long getId() {
        return id;
    }
}
