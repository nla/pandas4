package pandas.social.generic;

import java.net.URL;

public record Account(
        String username,
        String displayName,
        URL avatar
) {
}
