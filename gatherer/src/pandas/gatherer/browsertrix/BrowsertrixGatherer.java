package pandas.gatherer.browsertrix;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pandas.gather.*;
import pandas.gatherer.core.*;
import pandas.gatherer.repository.Repository;
import pandas.search.FileSearcher;
import pandas.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
    private final KubernetesClient kubeClient;
    private volatile boolean shutdown;
    private final Set<Integer> NORMAL_EXIT_CODES = Set.of(
            BrowsertrixExit.SUCCESS.code(),
            BrowsertrixExit.FAILED.code(), // incl. DNS lookup failure
            BrowsertrixExit.SIGNAL_INTERRUPTED.code(),
            BrowsertrixExit.SIZE_LIMIT.code(),
            BrowsertrixExit.TIME_LIMIT.code());
    private final static List<String> BEHAVIOR_SCRIPTS = List.of("bsky.js");
    private final static Path BEHAVIORS_DIR = unpackBehaviors();
    private String version;

    private static Path unpackBehaviors() {
        try {
            Path tempDir = Files.createTempDirectory("pandas-browsertrix-behaviors",
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x")));
            tempDir.toFile().deleteOnExit();

            for (String script : BEHAVIOR_SCRIPTS) {
                Path path = tempDir.resolve(script);
                try (InputStream stream = Objects.requireNonNull(BrowsertrixGatherer.class.getResourceAsStream(script),
                        "browsertrix behavior " + script + " not found")) {
                    Files.copy(stream, path);
                }
                path.toFile().deleteOnExit();
            }

            return tempDir;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to unpack browsertrix behaviors", e);
        }
    }

    public BrowsertrixGatherer(BrowsertrixConfig config, InstanceService instanceService, PywbService pywbService, WorkingArea workingArea, Repository repository, ThumbnailGenerator thumbnailGenerator) {
        this.config = config;
        this.instanceService = instanceService;
        this.pywbService = pywbService;
        this.workingArea = workingArea;
        this.repository = repository;
        this.thumbnailGenerator = thumbnailGenerator;
        this.kubeClient = config.isKubeEnabled() ? new KubernetesClientBuilder().build() : null;
    }

    @Override
    public String getGatherMethod() {
        return GatherMethod.BROWSERTRIX;
    }

    private List<String> buildCrawlerArguments(Instance instance) {
        TitleGather titleGather = instance.getTitle().getGather();
        int depth = -1;
        Scope scope = titleGather.getScope();
        if (scope != null && scope.getDepth() != null) {
            depth = scope.getDepth();
        }

        var args = new ArrayList<String>();
        args.addAll(List.of("crawl", "--id", instance.getHumanId(), "-c", instance.getBrowsertrixCollectionName(), "--combinewarc",
                "--logging", "none",
                "--saveState", "always",
                "--customBehaviors", "./.behaviors/",
                "--depth", String.valueOf(depth)));

        if (scope != null && scope.isIncludeSubdomains()) {
            args.add("--scopeType");
            args.add("domain");
        } else {
            args.add("--scopeType");
            args.add("prefix");
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

            if (profile.getBrowsertrixConfig() != null && !profile.getBrowsertrixConfig().isBlank()) {
                args.addAll(Strings.shellSplit(profile.getBrowsertrixConfig()));
            }
        }

        args.add("--timeLimit");
        args.add(String.valueOf(timeLimit));

        args.add("--sizeLimit");
        args.add(String.valueOf(sizeLimit));

        if (!Strings.isNullOrBlank(config.getUserAgentSuffix())) {
            args.add("--userAgentSuffix");
            args.add(config.getUserAgentSuffix());
        }

        // include external pdf files
        args.add("--include");
        args.add("^https?://[^?#]+\\.pdf$");

        // include linked google docs
        args.add("--include");
        args.add("^https://docs\\.google\\.com/(?:document|spreadsheets|presentation|drawings|form|file)/d/[^/]+/edit$");

        for (var seed : instance.getTitle().getAllSeeds()) {
            if (!seed.startsWith("http://") && !seed.startsWith("https://")) {
                log.warn("Ignoring non http/https seed: {}", seed);
                continue;
            }
            args.add("--url");
            args.add(seed);
        }
        return args;
    }

    @Override
    public int gather(Instance instance) throws Exception {
        Path workingDir = workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString());

        Path pywbDir = pywbService.directoryFor(instance);
        if (!Files.isSymbolicLink(pywbDir)) {
            if (Files.isDirectory(pywbDir)) PathUtils.deleteDirectory(pywbDir);
            Files.deleteIfExists(pywbDir);
            Files.createDirectories(pywbDir.getParent());
            Files.createSymbolicLink(pywbDir, workingDir.resolve("collections").resolve(instance.getBrowsertrixCollectionName()).toAbsolutePath());
        }

        if (config.isKubeEnabled()) {
            return gatherKube(instance, workingDir);
        } else {
            return gatherPodman(instance, workingDir);
        }
    }

    private int gatherPodman(Instance instance, Path workingDir) throws IOException, InterruptedException, GatherException {
        Path logFile = workingDir.resolve("stdio.log");

        var command = new ArrayList<>(List.of("podman", "run", "--rm",
                "-v", workingDir + ":/crawls/",
                "-v", BEHAVIORS_DIR + ":/behaviors/:z"));
        if (config.getPodmanOptions() != null) {
            command.addAll(Arrays.asList(config.getPodmanOptions().split(" ")));
        }
        command.add(config.getImage());
        command.addAll(buildCrawlerArguments(instance));

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
                if (shutdown || !instance.getState().equals(State.GATHERING)) {
                    return -1; // Return -1 for early termination
                }
            }

            int exitValue = process.exitValue();
            log.info(getGatherMethod() + " {} returned {}", instance.getHumanId(), exitValue);
            if (!NORMAL_EXIT_CODES.contains(exitValue)) {
                System.err.println(Files.readString(logFile));
                throw new GatherException("Browsertrix exited with status " + exitValue, exitValue);
            }
            return exitValue;
        } finally {
            process.destroy();
            if (!process.waitFor(shutdown ? 30 : 120, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        }
    }

    private int gatherKube(Instance instance, Path workingDir) throws IOException, GatherException {
        String jobId = "pandas-instance-" + instance.getId();
        Path logFile = workingDir.resolve("stdio.log");

        // Copy behaviors to the working directory so the job can access them
        Path behaviorDir = workingDir.resolve(".behaviors");
        if (!Files.exists(behaviorDir)) {
            Files.createDirectories(behaviorDir);
            for (String script : BEHAVIOR_SCRIPTS) {
                Files.copy(BEHAVIORS_DIR.resolve(script), behaviorDir.resolve(script));
            }
        }

        Job job = kubeClient.batch().v1().jobs().inNamespace(config.getKubeNamespace()).withName(jobId).get();
        if (job == null) {
            log.info("Starting browsertrix-crawler job {} for instance {}", jobId, instance.getHumanId());
            String subPath = workingArea.getPath().relativize(workingDir).toString();

            Job jobTemplate;
            try (InputStream is = config.getKubeJobConfig() != null
                    ? Files.newInputStream(config.getKubeJobConfig())
                    : BrowsertrixGatherer.class.getResourceAsStream("/kube-job.yaml")) {
                if (is == null) throw new IOException("kube-job.yaml not found");
                jobTemplate = kubeClient.batch().v1().jobs().load(is).item();
            }

            var container = jobTemplate.getSpec().getTemplate().getSpec().getContainers().get(0);
            String mountPath = container.getVolumeMounts().get(0).getMountPath();

            job = new JobBuilder(jobTemplate)
                    .editMetadata()
                        .withName(jobId)
                        .addToLabels("pandas/instance-id", instance.getId().toString())
                    .endMetadata()
                    .editSpec()
                        .editTemplate()
                            .editSpec()
                                .editFirstContainer()
                                    .withArgs(buildCrawlerArguments(instance))
                                    .withWorkingDir(mountPath + "/" + subPath)
                                .endContainer()
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                    .build();
            kubeClient.batch().v1().jobs().inNamespace(config.getKubeNamespace()).resource(job).create();
        } else {
            log.info("Resuming monitoring of browsertrix-crawler job {} for instance {}", jobId, instance.getHumanId());
        }

        try {
            return monitorKubeJob(instance, jobId, logFile);
        } finally {
            if (instanceService.refresh(instance).getState() != State.GATHERING) {
                kubeClient.batch().v1().jobs().inNamespace(config.getKubeNamespace()).withName(jobId).delete();
            }
        }
    }

    private int monitorKubeJob(Instance instance, String jobId, Path logFile) throws IOException, GatherException {
        Pod pod = null;
        while (pod == null) {
            var pods = kubeClient.pods().inNamespace(config.getKubeNamespace()).withLabel("job-name", jobId).list().getItems();
            if (!pods.isEmpty()) {
                pod = pods.get(pods.size() - 1);
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return -1;
                }
            }
        }

        try (LogWatch logWatch = kubeClient.pods().inNamespace(config.getKubeNamespace()).withName(pod.getMetadata().getName())
                .watchLog(Files.newOutputStream(logFile, APPEND, CREATE))) {
            while (true) {
                Job job = kubeClient.batch().v1().jobs().inNamespace(config.getKubeNamespace()).withName(jobId).get();
                if (job == null) {
                    log.warn("Job {} disappeared", jobId);
                    return -1;
                }

                if (job.getMetadata().getDeletionTimestamp() != null) {
                    log.info("Job {} is being deleted", jobId);
                    return -1;
                }

                if (job.getStatus() != null && job.getStatus().getCompletionTime() != null) {
                    int exitCode = getExitCode(jobId);
                    log.info("Job {} finished with exit code {}", jobId, exitCode);
                    if (!NORMAL_EXIT_CODES.contains(exitCode)) {
                        throw new GatherException("Browsertrix exited with status " + exitCode, exitCode);
                    }
                    kubeClient.batch().v1().jobs().inNamespace(config.getKubeNamespace()).withName(jobId).delete();
                    return exitCode;
                }

                if (job.getStatus() != null && job.getStatus().getFailed() != null && job.getStatus().getFailed() > 0) {
                    int exitCode = getExitCode(jobId);
                    log.info("Job {} failed with exit code {}", jobId, exitCode);
                    kubeClient.batch().v1().jobs().inNamespace(config.getKubeNamespace()).withName(jobId).delete();
                    throw new GatherException("Browsertrix job failed", exitCode);
                }

                instance = instanceService.refresh(instance);
                if (shutdown || !instance.getState().equals(State.GATHERING)) {
                    return -1;
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return -1;
                }
            }
        }
    }

    private int getExitCode(String jobId) {
        var pods = kubeClient.pods().inNamespace(config.getKubeNamespace()).withLabel("job-name", jobId).list().getItems();
        if (pods.isEmpty()) return -1;
        Pod pod = pods.get(pods.size() - 1);
        if (pod.getStatus() == null || pod.getStatus().getContainerStatuses() == null || pod.getStatus().getContainerStatuses().isEmpty()) return -1;
        ContainerStatus status = pod.getStatus().getContainerStatuses().get(0);
        if (status.getState() != null && status.getState().getTerminated() != null) {
            return status.getState().getTerminated().getExitCode();
        }
        return -1;
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
    public void postprocess(Instance instance) throws IOException {
        pywbService.reindex(instance);
        thumbnailGenerator.generateReplayThumbnail(instance, pywbService.replayUrlFor(instance));

        Path workingDir = workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString());
        (new FileSearcher(workingDir.resolve("fileindex"))).indexRecursively(workingDir);
    }

    @Override
    public void archive(Instance instance) throws IOException, InterruptedException {
        Path workingDir = workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString());
        Path collectionDir = workingDir.resolve("collections").resolve(instance.getBrowsertrixCollectionName());
        List<Path> warcs;
        try (Stream<Path> stream = Files.list(collectionDir)) {
            warcs = stream.filter(f -> f.getFileName().toString().endsWith(".warc.gz"))
                    .map(this::renameWarcIfNecessary)
                    .toList();
        }
        var artifacts = Stream.of(collectionDir.resolve("pages").resolve("pages.jsonl"),
                        workingDir.resolve("stdio.log")).filter(Files::exists).toList();
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
    public synchronized String version() throws IOException {
        if (version != null) return version;

        if (config.isKubeEnabled()) {
            String podName = "browsertrix-version-check-" + UUID.randomUUID();
            Pod pod = kubeClient.pods().inNamespace(config.getKubeNamespace()).resource(new PodBuilder()
                    .withNewMetadata().withName(podName).endMetadata()
                    .withNewSpec()
                    .withRestartPolicy("Never")
                    .addNewContainer()
                    .withName("version-check")
                    .withImage(config.getImage())
                    .withArgs("crawl", "--version")
                    .endContainer()
                    .endSpec()
                    .build()).create();
            try {
                kubeClient.pods().inNamespace(config.getKubeNamespace()).withName(podName).waitUntilReady(1, TimeUnit.MINUTES);
                version = kubeClient.pods().inNamespace(config.getKubeNamespace()).withName(podName).getLog();
                return version;
            } finally {
                kubeClient.pods().inNamespace(config.getKubeNamespace()).withName(podName).delete();
            }
        }

        Process process = new ProcessBuilder("podman", "run", "--rm", config.getImage(), "crawl", "--version")
                .inheritIO()
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start();
        try {
            version = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return version;
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
