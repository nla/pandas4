package pandas.browser;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for the BrowserPool.
 *
 * @param logging whether to enable browser debug logging
 * @param options command-line options to pass to the browser executable
 * @param executable the path to the browser executable
 * @param limit the maximum number of browsers to keep in the pool
 */
@ConfigurationProperties("browser")
public record BrowserProperties(boolean logging, List<String> options, String executable, int limit) {
    private static final List<String> DEFAULT_EXECUTABLES = List.of("chromium-browser", "chromium", "google-chrome",
            "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
            "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
            "/usr/lib64/chromium-browser/headless_shell");

    public BrowserProperties {
        if (options == null) {
            options = List.of();
        } else if (options.size() == 1 && options.get(0).contains(" ")) {
            options = Arrays.stream(options.get(0).trim().split("\\s+")).toList();
        } else {
            options = List.copyOf(options);
        }
        limit = limit == 0 ? 4 : limit;
    }

    public static BrowserProperties defaults() {
        return new BrowserProperties(false, null, null, 0);
    }

    public List<String> executablesOrDefault() {
        return executable != null ? List.of(executable) : DEFAULT_EXECUTABLES;
    }
}