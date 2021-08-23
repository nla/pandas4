package pandas.gatherer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import pandas.gather.Instance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class PywbService implements DisposableBean {
    private final Logger log = LoggerFactory.getLogger(PywbService.class);
    private final Process process;
    private final Config config;

    public PywbService(Config config) throws IOException {
        this.config = config;
        Path working = config.getPywbDataDir().resolve("nobanner");
        Files.createDirectories(working.resolve("templates"));

        Files.write(working.resolve("config.yaml"), "collections_root: ../collections\nframed_replay: false\n".getBytes(UTF_8));
        Files.write(working.resolve("templates").resolve("banner.html"), new byte[0]);

        process = new ProcessBuilder("pywb", "-b", "127.0.0.1", "-p", String.valueOf(config.getPywbPort()))
                .directory(working.toFile())
                .inheritIO()
                .start();
    }

    public String collectionFor(Instance instance) {
        return instance.getTitle().getPi() + "-" + instance.getDateString();
    }

    public Path directoryFor(Instance instance) {
        return config.getPywbDataDir().resolve("collections").resolve(collectionFor(instance));
    }

    public String replayUrlFor(Instance instance) {
        return "http://127.0.0.1:" + config.getPywbPort() + "/" + collectionFor(instance) + "/mp_/" +
                instance.getGatheredUrl();
    }

    public void reindex(Instance instance) throws IOException {
        try {
            new ProcessBuilder("wb-manager", "reindex", collectionFor(instance))
                    .directory(config.getPywbDataDir().toFile())
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (InterruptedException e) {
            log.warn("Interrupted indexing", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        process.destroy();
        if (!process.waitFor(2, TimeUnit.SECONDS)) {
            process.destroyForcibly();
        }
    }
}
