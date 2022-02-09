package pandas.collection;

public class CollectionBrief {
    private final long id;
    private final String name;
    private final long titleCount;

    public CollectionBrief(long id, String name, long titleCount) {
        this.id = id;
        this.name = name;
        this.titleCount = titleCount;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getTitleCount() {
        return titleCount;
    }
}
