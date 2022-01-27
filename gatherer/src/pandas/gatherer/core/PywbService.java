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

@Service
public class PywbService implements DisposableBean {
    private final Logger log = LoggerFactory.getLogger(PywbService.class);
    private final Process process;
    private final Config config;
    private final PywbConfig pywbConfig;

    public PywbService(Config config, PywbConfig pywbConfig) throws IOException {
        this.config = config;
        this.pywbConfig = pywbConfig;
        Path working = config.getPywbDataDir().resolve("nobanner");
        Files.createDirectories(working.resolve("templates"));

        Files.writeString(working.resolve("config.yaml"), "collections_root: ../collections\nframed_replay: false\n");
        Files.write(working.resolve("templates").resolve("banner.html"), new byte[0]);

        process = new ProcessBuilder("pywb", "-b", pywbConfig.getBindAddress(),
                "-p", String.valueOf(pywbConfig.getPort()))
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
        String address = pywbConfig.getBindAddress();
        if (address.equals("0.0.0.0")) {
            address = "127.0.0.1";
        }
        return "http://" + address + ":" + pywbConfig.getPort() + "/" + collectionFor(instance) + "/mp_/" +
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
