package pandas.social;

import java.net.URL;

public record Account(
        String username,
        String displayName,
        URL avatar
) {
}
