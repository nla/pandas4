package pandas.collection;

public class SubjectBrief {
    private final long id;
    private final String name;
    private final long titleCount;
    private final long collectionCount;

    public SubjectBrief(long id, String name, long titleCount, long collectionCount) {
        this.id = id;
        this.name = name;
        this.titleCount = titleCount;
        this.collectionCount = collectionCount;
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

    public long getCollectionCount() {
        return collectionCount;
    }
}
