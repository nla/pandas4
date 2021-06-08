package pandas.crawlconfig;

public class Seed {
    private final String url;
    private final Scope scope;

    public Seed(String url) {
        this.url = url;
        scope = Scope.AUTO;
    }

    public Seed(String url, Scope scope) {
        this.url = url;
        this.scope = scope;
    }

    public String getUrl() {
        return url;
    }

    public Scope getScope() {
        return scope;
    }
}
