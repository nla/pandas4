package pandas.gather;

import java.util.Arrays;
import java.util.Optional;

/** Bot-blocking services recognised in captured HTTP responses. */
public enum BotBlocker {
    AKAMAI("Akamai"),
    ANUBIS("Anubis"),
    CLOUDFLARE("Cloudflare"),
    DATADOME("DataDome"),
    INCAPSULA("Incapsula");

    private final String displayName;

    BotBlocker(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static Optional<BotBlocker> fromIndexValue(String value) {
        return Arrays.stream(values())
                .filter(blocker -> blocker.name().equals(value))
                .findFirst();
    }
}
