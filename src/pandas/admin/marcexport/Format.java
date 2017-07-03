package pandas.admin.marcexport;

public enum Format {
    SERIAL('s'), MONO('m'), INTEGRATING('i');

    private final char marcCode;

    Format(char m) {
        this.marcCode = m;
    }

    static Format byId(int id) {
        return Format.values()[id - 1];
    }

    int id() {
        return ordinal() + 1;
    }

    public char leaderCode() {
        return marcCode;
    }
}
