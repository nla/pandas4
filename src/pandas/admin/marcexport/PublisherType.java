package pandas.admin.marcexport;

public enum PublisherType {
    GOVERNMENT, ORGANISATION, EDUCATION, COMMERCIAL, PERSONAL, OTHER;

    static PublisherType byId(int id) {
        return PublisherType.values()[id - 1];
    }

    int id() {
        return ordinal() + 1;
    }
}
