package pandas.browser;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties("browser")
public record BrowserProperties(boolean logging, List<String> options, String executable) {
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
    }

    public static BrowserProperties defaults() {
        return new BrowserProperties(false, null, null);
    }

    public List<String> executablesOrDefault() {
        return executable != null ? List.of(executable) : DEFAULT_EXECUTABLES;
    }
}