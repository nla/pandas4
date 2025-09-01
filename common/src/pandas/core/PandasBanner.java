package pandas.core;

import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

public class PandasBanner implements Banner {
    private static final String BANNER = """
             ----------------------------------------------------------------------
            |                                    ._________.                       |
            |  %-34s |||||||||    NATIONAL LIBRARY    |
            |  %-34s IIIIIIIII      OF AUSTRALIA      |
            |                                    /=========\\                       |
             ----------------------------------------------------------------------
            """;

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        Properties gitProperties = new Properties();
        try (var stream = getClass().getResourceAsStream("/git.properties")) {
            if (stream != null) gitProperties.load(stream);
        } catch (IOException ignore) {
        }
        String app = environment.getProperty("spring.application.name", "PANDAS");
        String version = gitProperties.getProperty("git.commit.id.describe", "");
        String branch = gitProperties.getProperty("git.branch", "master");
        if (!branch.equals("master")) {
            app += " [" + branch + "]";
            app = app.substring(0, Math.min(app.length(), 34));
        }
        out.printf(BANNER, app, version);
        out.flush();
    }
}
