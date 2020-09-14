package pandas.admin.search;

public class FacetEntry {
    private final String name;
    private final String href;
    private final long count;

    public FacetEntry(String name, String href, long count) {
        this.name = name;
        this.href = href;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public String getHref() {
        return href;
    }

    public long getCount() {
        return count;
    }
}
