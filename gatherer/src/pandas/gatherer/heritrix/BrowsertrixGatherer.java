package pandas.gatherer.heritrix;

import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pandas.gather.*;
import pandas.gatherer.core.*;
import pandas.gatherer.repository.Repository;
import pandas.util.Strings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

@Component
public class BrowsertrixGatherer implements Backend {
    private static final Logger log = LoggerFactory.getLogger(BrowsertrixGatherer.class);
    private final BrowsertrixConfig config;
    private final InstanceService instanceService;
    private final PywbService pywbService;
    private final WorkingArea workingArea;
    private final Repository repository;
    private final ThumbnailGenerator thumbnailGenerator;
    private volatile boolean shutdown;
    private final static int EXIT_OK = 0;
    private final static int EXIT_INTERRUPTED = 11;

    public BrowsertrixGatherer(BrowsertrixConfig config, InstanceService instanceService, PywbService pywbService, WorkingArea workingArea, Repository repository, ThumbnailGenerator thumbnailGenerator) {
        this.config = config;
        this.instanceService = instanceService;
        this.pywbService = pywbService;
        this.workingArea = workingArea;
        this.repository = repository;
        this.thumbnailGenerator = thumbnailGenerator;
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
            Files.createSymbolicLink(pywbDir, workingDir.resolve("collections").resolve(instance.getBrowsertrixCollectionName()).toAbsolutePath());
        }

        Path logFile = workingDir.resolve("stdio.log");

        int depth = -1;

        TitleGather titleGather = instance.getTitle().getGather();
        Scope scope = titleGather.getScope();
        if (scope != null && scope.getDepth() != null) {
            depth = scope.getDepth();
        }

        var command = new ArrayList<String>();
        command.addAll(List.of("podman", "run", "--rm", "-v", workingDir + ":/crawls/"));
        if (config.getPodmanOptions() != null) {
            command.addAll(Arrays.asList(config.getPodmanOptions().split(" ")));
        }
        command.addAll(List.of(config.getImage(),
                "crawl", "--id", instance.getHumanId(), "-c", instance.getBrowsertrixCollectionName(), "--combinewarc",
                "--logging", "none",
                "--saveState", "always",
                "--depth", String.valueOf(depth)));

        if (scope != null && scope.isIncludeSubdomains()) {
            command.add("--scopeType");
            command.add("domain");
        } else {
            command.add("--scopeType");
            command.add("prefix");
        }

        long timeLimit = titleGather.getCrawlTimeLimitSeconds();
        long sizeLimit = config.getDefaultCrawlLimitBytes();

        Profile profile = titleGather.getActiveProfile();
        if (profile != null) {
            if (profile.getCrawlLimitSeconds() != null) {
                timeLimit = profile.getCrawlLimitSeconds();
            }

            if (profile.getCrawlLimitBytes() != null) {
                sizeLimit = profile.getCrawlLimitBytes();
            }
        }

        command.add("--timeLimit");
        command.add(String.valueOf(timeLimit));

        command.add("--sizeLimit");
        command.add(String.valueOf(sizeLimit));


        if (!Strings.isNullOrBlank(config.getUserAgentSuffix())) {
            command.add("--userAgentSuffix");
            command.add(config.getUserAgentSuffix());
        }

        // include external pdf files
        command.add("--include");
        command.add("^https?://[^?#]+\\.pdf$");

        // include linked google docs
        command.add("--include");
        command.add("^https://docs\\.google\\.com/(?:document|spreadsheets|presentation|drawings|form|file)/d/[^/]+/edit$");

        for (var seed : instance.getTitle().getAllSeeds()) {
            if (!seed.startsWith("http://") && !seed.startsWith("https://")) {
                log.warn("Ignoring non http/https seed: {}", seed);
                continue;
            }
            command.add("--url");
            command.add(seed);
        }
        Files.writeString(logFile, encodeShellCommandForLogging(command) + "\n", APPEND, CREATE);
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

            int exitValue = process.exitValue();
            log.info(getGatherMethod() + " {} returned {}", instance.getHumanId(), exitValue);
            if (exitValue != EXIT_OK && exitValue != EXIT_INTERRUPTED) {
                System.err.println(Files.readString(logFile));
                throw new GatherException("Browsertrix exited with status " + exitValue);
            }
        } finally {
            process.destroy();
            if (!process.waitFor(shutdown ? 30 : 120, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        }
    }

    private static final Pattern SHELL_SPECIAL_CHAR = Pattern.compile("[|&;<>()$`\\\\\"' \t\r\n*?\\[#~=%]");

    static String encodeShellCommandForLogging(List<String> args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            if (!builder.isEmpty()) builder.append(' ');
            builder.append(SHELL_SPECIAL_CHAR.matcher(arg).replaceAll("\\\\$0"));
        }
        return builder.toString();
    }

    @Override
    public void postprocess(Instance instance) throws IOException, InterruptedException {
        pywbService.reindex(instance);
        thumbnailGenerator.generateReplayThumbnail(instance, pywbService.replayUrlFor(instance));
    }

    @Override
    public void archive(Instance instance) throws IOException, InterruptedException {
        Path workingDir = workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString());
        Path collectionDir = workingDir.resolve("collections").resolve(instance.getBrowsertrixCollectionName());
        var warcs = Files.list(collectionDir)
                .filter(f -> f.getFileName().toString().endsWith(".warc.gz"))
                .map(this::renameWarcIfNecessary)
                .toList();
        var artifacts = List.of(collectionDir.resolve("pages").resolve("pages.jsonl"),
                        workingDir.resolve("stdio.log")).stream().filter(Files::exists).toList();
        repository.storeArtifactPaths(instance, artifacts);
        repository.storeWarcs(instance, warcs);
        delete(instance);
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
        Path pywbDir = pywbService.directoryFor(instance);
        if (Files.isSymbolicLink(pywbDir)) {
            Files.deleteIfExists(pywbDir);
        } else if (Files.isDirectory(pywbDir)) {
            workingArea.deleteRecursivelyIfExists(pywbDir);
        }
    }

    @Override
    public String version() throws IOException {
        Process process = new ProcessBuilder("podman", "run", "--rm", config.getImage(), "crawl", "--version")
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
        return config.getWorkers();
    }
}
