package pandas.gatherer.heritrix;

import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pandas.gather.GatherMethod;
import pandas.gather.Instance;
import pandas.gather.InstanceService;
import pandas.gather.State;
import pandas.gatherer.core.Backend;
import pandas.gatherer.core.GatherException;
import pandas.gatherer.core.PywbService;
import pandas.gatherer.core.WorkingArea;
import pandas.gatherer.repository.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.stream.Collectors.toList;

@Component
public class BrowsertrixGatherer implements Backend {
    private static final Logger log = LoggerFactory.getLogger(BrowsertrixGatherer.class);
    private final BrowsertrixConfig config;
    private final InstanceService instanceService;
    private final PywbService pywbService;
    private final WorkingArea workingArea;
    private final Repository repository;
    private volatile boolean shutdown;

    public BrowsertrixGatherer(BrowsertrixConfig config, InstanceService instanceService, PywbService pywbService, WorkingArea workingArea, Repository repository) {
        this.config = config;
        this.instanceService = instanceService;
        this.pywbService = pywbService;
        this.workingArea = workingArea;
        this.repository = repository;
    }

    @Override
    public String getGatherMethod() {
        return GatherMethod.BROWSERTRIX;
    }

    @Override
    public void gather(Instance instance) throws Exception {
        Path workingDir = workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString());

        Path pywbDir = pywbService.directoryFor(instance);
        if (!Files.isSymbolicLink(pywbDir)) {
            if (Files.isDirectory(pywbDir)) PathUtils.deleteDirectory(pywbDir);
            Files.deleteIfExists(pywbDir);
            Files.createDirectories(pywbDir.getParent());
            Files.createSymbolicLink(pywbDir, workingDir.resolve("collections").resolve(collectionName(instance)).toAbsolutePath());
        }

        Path logFile = workingDir.resolve("stdio.log");
        var command = new ArrayList<String>();
        command.addAll(List.of("podman", "run", "--rm", "-v", workingDir + ":/crawls/"));
        if (config.getPodmanOptions() != null) {
            command.addAll(Arrays.asList(config.getPodmanOptions().split(" ")));
        }
        command.addAll(List.of("webrecorder/browsertrix-crawler",
                "crawl", "--id", instance.getHumanId(), "-c", collectionName(instance), "--combinewarc",
                "--generatecdx", "--logging", "pywb"));
        for (var seed : instance.getTitle().getAllSeeds()) {
            if (!seed.startsWith("http://") && !seed.startsWith("https://")) {
                log.warn("Ignoring non http/https seed: {}", seed);
                continue;
            }
            command.add("--url");
            command.add(seed);
        }
        Files.writeString(logFile, String.join(" ", command) + "\n", APPEND, CREATE);
        log.info("Executing {}", String.join(" ", command));

        Process process = new ProcessBuilder(command)
                .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()))
                .redirectErrorStream(true)
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .directory(workingDir.toFile())
                .start();
        try {
            while (!process.waitFor(1, TimeUnit.SECONDS)) {
                instance = instanceService.refresh(instance);
                if (shutdown || !instance.getState().getName().equals(State.GATHERING)) {
                    return;
                }
            }

            log.info("HTTrack {} returned {}", instance.getHumanId(), process.exitValue());
            if (process.exitValue() != 0) {
                System.err.println(Files.readString(logFile));
                throw new GatherException("Browsertrix exited with status " + process.exitValue());
            }
        } finally {
            process.destroy();
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        }
    }

    private String collectionName(Instance instance) {
        return instance.getHumanId().replace('.', '-');
    }

    @Override
    public void postprocess(Instance instance) throws IOException, InterruptedException {
    }

    @Override
    public void archive(Instance instance) throws IOException, InterruptedException {
        Path workingDir = workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString());
        Path collectionDir = workingDir.resolve("collections").resolve(collectionName(instance));
        var warcFiles = Files.list(collectionDir)
                .filter(f -> f.getFileName().toString().endsWith(".warc.gz"))
                .map(this::renameWarcIfNecessary)
                .collect(toList());
        Path pagesLogFile = collectionDir.resolve("pages").resolve("pages.jsonl");
        if (Files.exists(pagesLogFile)) {
            repository.storeArtifactPaths(instance, List.of(pagesLogFile));
        }
        repository.storeWarcs(instance, warcFiles);
    }

    // rename warcs from nla.arc-12345-20100830-1234_0.warc.gz
    //                to nla.arc-12345-20100830-1234-0.warc.gz
    private Path renameWarcIfNecessary(Path path) throws UncheckedIOException {
        try {
            String oldName = path.getFileName().toString();
            String newName = oldName.replaceFirst("^nla-arc", "nla.arc")
                    .replace('_', '-');
            if (oldName.equals(newName)) return path;
            return Files.move(path, path.resolveSibling(newName));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public void delete(Instance instance) throws IOException {
        workingArea.deleteInstance(instance.getTitle().getPi(), instance.getDateString());
    }

    @Override
    public String version() throws IOException {
        Process process = new ProcessBuilder("podman", "run", "--rm", "webrecorder/browsertrix-crawler", "crawl", "--version")
                .inheritIO()
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start();
        try {
            return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            process.destroy();
            try {
                if (!process.waitFor(30, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                process.destroyForcibly();
            }
        }
    }

    @Override
    public int getWorkerCount() {
        return 1;
    }
}
