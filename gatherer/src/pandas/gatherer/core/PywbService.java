package pandas.gatherer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import pandas.gather.Instance;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class PywbService implements DisposableBean {
    private final Logger log = LoggerFactory.getLogger(PywbService.class);
    private final Process process;
    private final Config config;
    private final PywbConfig pywbConfig;
    private boolean started;

    public PywbService(Config config, PywbConfig pywbConfig) throws IOException {
        this.config = config;
        this.pywbConfig = pywbConfig;
        Path working = config.getPywbDataDir().resolve("nobanner");
        Files.createDirectories(working.resolve("templates"));

        Files.writeString(working.resolve("config.yaml"), "collections_root: ../collections\nframed_replay: false\n");
        Files.write(working.resolve("templates").resolve("banner.html"), new byte[0]);

        process = new ProcessBuilder("pywb", "-b", pywbConfig.getBindAddress(),
                "-p", String.valueOf(pywbConfig.getPort()), "--record")
                .directory(working.toFile())
                .inheritIO()
                .start();
    }

    @SuppressWarnings("BusyWait")
    private synchronized void waitUntilStarted() {
        if (started) return;
        for (int tries = 0; ; tries++) {
            if (!process.isAlive()) {
                throw new RuntimeException("Pywb exited with status " + process.exitValue());
            }
            try (SocketChannel socket = SocketChannel.open()) {
                socket.connect(new InetSocketAddress(pywbConfig.getBindAddress(), pywbConfig.getPort()));
                break;
            } catch (ConnectException e) {
                if (tries >= 600) {
                    throw new UncheckedIOException("Timeout waiting for pywb to start", e);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    throw new RuntimeException("Interrupted waiting for pywb to start", ex);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        started = true;
    }

    public String collectionFor(Instance instance) {
        return instance.getTitle().getPi() + "-" + instance.getDateString();
    }

    public Path directoryFor(Instance instance) {
        return config.getPywbDataDir().resolve("collections").resolve(collectionFor(instance));
    }

    public String replayUrlFor(Instance instance) {
        waitUntilStarted();
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
