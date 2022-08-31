package pandas.collection;

import java.io.Serializable;

public record UrlStatsId(String site, String contentType, int year) implements Serializable {
}
