package pandas.gatherer.heritrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class HeritrixProcess implements Closeable {
    private final Logger log = LoggerFactory.getLogger(HeritrixProcess.class);
    private final Process process;
    private final String password;
    private final int port;
    private final Path stdio;

    public HeritrixProcess(Path home, Path workingDir, int port, String password) throws IOException {
        if (password == null) password = UUID.randomUUID().toString();
        this.password = password;
        this.port = port;
        String javaExe = Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java").toString();
        this.stdio = workingDir.resolve("stdio");
        String[] commandLine = {javaExe, "-cp", home.toAbsolutePath() + "/lib/*",
                "-Xmx512m",
                "org.archive.crawler.Heritrix",
                "-a", password,
                "-p", Integer.toString(port)};
        log.info("Launching {}", String.join(" ", commandLine));
        this.process = new ProcessBuilder(commandLine)
                .directory(workingDir.toFile())
                .redirectOutput(stdio.toFile())
                .redirectErrorStream(true)
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                close();
            } catch (IOException e) {
                log.warn("Error shutting down Heritrix", e);
            }
        }, "HeritrixProcess shutdown hook"));
    }

    public HeritrixClient getClient() {
        return new HeritrixClient("https://127.0.0.1:" + port + "/engine", "admin", password);
    }

    public Path getStdio() {
        return stdio;
    }

    @Override
    public void close() throws IOException {
        process.destroy();
        try {
            process.waitFor(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Timed out waiting for Heritrix to exit. Killing it.");
        }
        if (process.isAlive()) {
            process.destroyForcibly();
        }
    }
}
