package pandas.gatherer.heritrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import pandas.gather.*;
import pandas.gatherer.CrawlBeans;
import pandas.gatherer.core.*;
import pandas.gatherer.repository.Repository;

import java.io.IOException;
import java.net.SocketException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static pandas.gatherer.heritrix.HeritrixClient.State.*;

@Component
@ConditionalOnExpression("'${heritrix.url:}' != '' or '${heritrix.home:}' != ''")
public class HeritrixGatherer implements Backend {
    private static final Logger log = LoggerFactory.getLogger(HeritrixGatherer.class);
    private final HeritrixClient heritrix;
    private final HeritrixProcess process;
    private final WorkingArea workingArea;
    private final InstanceService instanceService;
    private final InstanceRepository instanceRepository;
    private final HeritrixConfig heritrixConfig;
    private final Repository repository;
    private final PywbService pywbService;
    private final ThumbnailGenerator thumbnailGenerator;
    private boolean shutdown;

    public HeritrixGatherer(Config config, HeritrixConfig heritrixConfig, WorkingArea workingArea, InstanceService instanceService, InstanceRepository instanceRepository, Repository repository, PywbService pywbService, ThumbnailGenerator thumbnailGenerator) throws IOException {
        this.workingArea = workingArea;
        this.heritrixConfig = heritrixConfig;
        if (heritrixConfig.getUrl() != null) {
            heritrix = new HeritrixClient(heritrixConfig.getUrl(), heritrixConfig.getUser(), heritrixConfig.getPassword());
            process = null;
        } else if (heritrixConfig.getHome() != null) {
            Path heritrixWorking = config.getWorkingDir().resolve("heritrix");
            Files.createDirectories(heritrixWorking);
            process = new HeritrixProcess(heritrixConfig.getHome(),
                    heritrixWorking, 18443, heritrixConfig.getPassword());
            heritrix = process.getClient();
        } else {
            throw new IllegalStateException("No heritrix.url or heritrix.home configured");
        }
        this.instanceService = instanceService;
        this.instanceRepository = instanceRepository;
        this.repository = repository;
        this.pywbService = pywbService;
        this.thumbnailGenerator = thumbnailGenerator;
    }

    @Override
    public int gather(Instance instance) throws Exception {
        // create heritrix dirs
        Path jobDir = jobDir(instance);
        Files.createDirectories(jobDir);
        CrawlBeans.writeConfig(instance, jobDir, heritrixConfig.getBindAddress());

        // create pywb dirs
        Path collDir = pywbService.directoryFor(instance);
        Path indexDir = workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString()).resolve("indexes");
        Files.createDirectories(collDir);
        Files.createDirectories(indexDir);
        createSymbolicLinkIfNotExists(collDir.resolve("archive"), jobDir(instance));
        createSymbolicLinkIfNotExists(collDir.resolve("indexes"), indexDir);

        String jobName = instance.getHumanId();
        try {
            while (!shutdown) {
                try {
                    launchJob(jobName, jobDir);
                    monitorJob(jobName, instance);
                    return 0;
                } catch (SocketException e) {
                    log.warn("Heritrix is down ({}), job {} is waiting for it to come back", e.getMessage(), jobName);
                    while (!shutdown) {
                        if (!instanceService.refresh(instance).getState().isGathering()) break;
                        Thread.sleep(1000);
                        try {
                            heritrix.getEngine();
                        } catch (IOException e2) {
                            continue;
                        }
                        break;
                    }
                    log.warn("Heritrix is up again, trying to relaunch job {}", jobName);
                }
            }
            return -1;
        } finally {
            if (shutdown) {
                log.info("Letting Heritrix crawl {} continue running after shutdown", jobName);
            } else {
                try {
                    heritrix.teardownJob(jobName);
                } catch (IOException e) {
                    log.error("Error tearing down Heritrix job {}", jobName, e);
                }
            }
        }
    }

    /**
     * Launches and unpauses a Heritrix job. If the job already exists and has any checkpoints attempts to resume the
     * latest one. If the job is already running or finished no action is taken.
     */
    private void launchJob(String jobName, Path jobDir) throws IOException, InterruptedException {
        heritrix.addJobDir(jobDir);

        HeritrixClient.State currentState = heritrix.getJob(jobName).crawlControllerState;
        if (currentState == RUNNING || currentState == FINISHED) {
            log.info("job {} already {}, assuming gatherer was restarted and resuming monitoring.", jobName, currentState);
            return;
        }

        heritrix.buildJob(jobName);
        var job = heritrix.getJob(jobName);
        String checkpoint = job.checkpointFiles.isEmpty() ? null : job.checkpointFiles.get(0);
        heritrix.launchJob(jobName, checkpoint);
        heritrix.waitForStateTransition(jobName, PREPARING, PAUSED);
        heritrix.unpauseJob(jobName);
    }

    /**
     * Continuously checks the status of a Heritrix job and the related instance,
     * stopping when the job finishes or the instance status changes (e.g. user cancels via UI).
     */
    private void monitorJob(String jobName, Instance instance) throws IOException, InterruptedException {
        while (!shutdown) {
            HeritrixClient.Job job = heritrix.getJob(jobName);
            instance = instanceService.refresh(instance);
            instanceService.updateGatherStats(instance.getId(), job.fileStats().fileCount(), job.fileStats().size());
            if (job.crawlControllerState != RUNNING) {
                log.info("job {} finished with state {}", jobName, job.crawlControllerState);
                return;
            }
            if (!instance.getState().isGathering()) {
                log.info("job {} unfinished but instance {} is {} so cancelling", jobName, instance.getId(),
                        instance.getState());
                return;
            }
            Thread.sleep(1000);
        }
    }

    private static void createSymbolicLinkIfNotExists(Path link, Path target) throws IOException {
        if (!Files.exists(link)) {
            Files.createSymbolicLink(link, target);
        }
    }

    @Override
    public void postprocess(Instance instance) throws IOException {
        Path seedsReportPath = jobDir(instance).resolve("latest").resolve("reports").resolve("seeds-report.txt");
        try (var reader = Files.newBufferedReader(seedsReportPath)) {
            instanceService.updateSeedStatuses(instance.getId(), InstanceSeed.parseHeritrixSeedReport(reader));
        } catch (IOException e) {
            log.warn("Error parsing " + seedsReportPath, e);
        }
        pywbService.reindex(instance);
        thumbnailGenerator.generateReplayThumbnail(instance, pywbService.replayUrlFor(instance));
    }

    @Override
    public void archive(Instance instance) throws IOException {
        Path jobDir = jobDir(instance);
        List<Path> warcs = new ArrayList<>();
        List<Artifact> artifacts = new ArrayList<>();
        var directoriesToIgnore = Set.of("scratch", "state", "action", "actions-done");
        Files.walkFileTree(jobDir, new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path path,
                                                     BasicFileAttributes basicFileAttributes) throws IOException {
                if (directoriesToIgnore.contains(path.getFileName().toString())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes basicFileAttributes) throws IOException {
                Path relpath = jobDir.relativize(file);
                if (Files.isSymbolicLink(file)) return FileVisitResult.CONTINUE;

                String filename = relpath.getFileName().toString();
                if (filename.endsWith(".lck")) return FileVisitResult.CONTINUE;

                log.debug("Artifact {}", filename);
                if (filename.endsWith(".warc.gz")) {
                    warcs.add(file);
                } else {
                    artifacts.add(new Artifact(relpath.toString(), file));
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                if (e instanceof NoSuchFileException) {
                    log.warn("File not found while walking tree archiving {}", path);
                    return FileVisitResult.CONTINUE;
                }
                throw e;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
                if (e != null) {
                    if (e instanceof NoSuchFileException) {
                        log.warn("File not found while walking tree archiving {}", path);
                        return FileVisitResult.CONTINUE;
                    }
                    throw e;
                }
                return FileVisitResult.CONTINUE;
            }
        });

        repository.storeWarcs(instance, warcs);
        repository.storeArtifacts(instance, artifacts);

        delete(instance);
    }

    @Override
    public void delete(Instance instance) throws IOException {
        workingArea.deleteInstance(instance.getTitle().getPi(), instance.getDateString());
        workingArea.deleteRecursivelyIfExists(pywbService.directoryFor(instance));
    }

    @Override
    public String version() throws IOException {
        return heritrix.getEngine().heritrixVersion;
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public int getWorkerCount() {
        return heritrixConfig.getWorkers();
    }

    private Path jobDir(Instance instance) {
        // we put an extra subdirectory level with the human id because Heritrix takes the name of the job
        // from the job directory name
        return workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString()).resolve(instance.getHumanId());
    }

    @Override
    public String getGatherMethod() {
        return GatherMethod.HERITRIX;
    }
}
