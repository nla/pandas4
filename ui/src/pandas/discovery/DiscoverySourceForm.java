package pandas.discovery;

import javax.validation.constraints.NotBlank;

import static info.freelibrary.util.StringUtils.trimToNull;

public class DiscoverySourceForm {
    @NotBlank
    private final String name;
    private final String url;

    // spider options
    private final String itemQuery;
    private final String itemNameQuery;
    private final String itemLinkQuery;
    private final String itemDescriptionQuery;
    private final String linkQuery;

    public static DiscoverySourceForm blank() {
        return new DiscoverySourceForm(null, null, null, null, null, null, null);
    }

    public DiscoverySourceForm(String name, String url, String itemQuery, String itemNameQuery, String itemLinkQuery, String itemDescriptionQuery, String linkQuery) {
        this.name = trimToNull(name);
        this.url = trimToNull(url);
        this.itemQuery = trimToNull(itemQuery);
        this.itemNameQuery = trimToNull(itemNameQuery);
        this.itemLinkQuery = trimToNull(itemLinkQuery);
        this.itemDescriptionQuery = trimToNull(itemDescriptionQuery);
        this.linkQuery = trimToNull(linkQuery);
    }

    public String name() {
        return name;
    }

    public String url() {
        return url;
    }

    public String itemQuery() {
        return itemQuery;
    }

    public String itemNameQuery() {
        return itemNameQuery;
    }

    public String itemLinkQuery() {
        return itemLinkQuery;
    }

    public String itemDescriptionQuery() {
        return itemDescriptionQuery;
    }

    public String linkQuery() {
        return linkQuery;
    }
}
