package pandas.gatherer.heritrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pandas.gather.GatherMethod;
import pandas.gather.Instance;
import pandas.gather.InstanceService;
import pandas.gather.State;
import pandas.gatherer.CrawlBeans;
import pandas.gatherer.core.*;
import pandas.gatherer.repository.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static pandas.gatherer.heritrix.HeritrixClient.State.*;

@Component
@ConditionalOnProperty("heritrix.url")
public class HeritrixGatherer implements Backend {
    private static final Logger log = LoggerFactory.getLogger(HeritrixGatherer.class);
    private final HeritrixClient heritrix;
    private final WorkingArea workingArea;
    private final InstanceService instanceService;
    private final Config config;
    private final HeritrixConfig heritrixConfig;
    private final Repository repository;
    private final PywbService pywbService;
    private final ThumbnailGenerator thumbnailGenerator;
    private boolean shutdown;

    public HeritrixGatherer(Config config, HeritrixConfig heritrixConfig, WorkingArea workingArea, InstanceService instanceService, Repository repository, PywbService pywbService, ThumbnailGenerator thumbnailGenerator) {
        this.workingArea = workingArea;
        this.config = config;
        this.heritrixConfig = heritrixConfig;
        heritrix = new HeritrixClient(heritrixConfig.getUrl(), heritrixConfig.getUser(), heritrixConfig.getPassword());
        this.instanceService = instanceService;
        this.repository = repository;
        this.pywbService = pywbService;
        this.thumbnailGenerator = thumbnailGenerator;
    }

    @Override
    public void gather(Instance instance) throws Exception {
        // create heritrix dirs
        Path jobDir = jobDir(instance);
        Files.createDirectories(jobDir);
        CrawlBeans.writeConfig(instance, jobDir, config.getGathererBindAddress());

        // create pywb dirs
        Path collDir = pywbService.directoryFor(instance);
        Path indexDir = workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString()).resolve("indexes");
        Files.createDirectories(collDir);
        Files.createDirectories(indexDir);
        createSymbolicLinkIfNotExists(collDir.resolve("archive"), jobDir(instance));
        createSymbolicLinkIfNotExists(collDir.resolve("indexes"), indexDir);

        heritrix.addJobDir(jobDir);

        try {
            if (heritrix.getJob(instance.getHumanId()).crawlControllerState == RUNNING) {
                log.info("job {} already RUNNING, assuming gatherer was restarted and resuming monitoring.", instance.getHumanId());
            } else {
                heritrix.buildJob(instance.getHumanId());
                heritrix.launchJob(instance.getHumanId());
                heritrix.waitForStateTransition(instance.getHumanId(), PREPARING, PAUSED);
                heritrix.unpauseJob(instance.getHumanId());
            }

            while (true) {
                HeritrixClient.Job job = heritrix.getJob(instance.getHumanId());
                instance = instanceService.refresh(instance);
                instanceService.updateGatherStats(instance, job.fileStats().fileCount(), job.fileStats().size());
                if (job.crawlControllerState != RUNNING ||
                        !instance.getState().getName().equals(State.GATHERING) ||
                        shutdown) {
                    break;
                }

                Thread.sleep(1000);
            }

        } finally {
            heritrix.teardownJob(instance.getHumanId());
        }
    }

    private static void createSymbolicLinkIfNotExists(Path link, Path target) throws IOException {
        if (!Files.exists(link)) {
            Files.createSymbolicLink(link, target);
        }
    }

    @Override
    public void postprocess(Instance instance) throws IOException {
        pywbService.reindex(instance);
        thumbnailGenerator.generateReplayThumbnail(instance, pywbService.replayUrlFor(instance));
    }

    @Override
    public void archive(Instance instance) throws IOException {
        Path jobDir = jobDir(instance);
        List<Path> warcs = new ArrayList<>();
        List<Artifact> artifacts = new ArrayList<>();
        for (Path file : Files.walk(jobDir).collect(toList())) {
            Path relpath = jobDir.relativize(file);
            if (Files.isDirectory(file)) continue;

            String filename = relpath.getFileName().toString();
            if (filename.endsWith(".lck")) continue;

            String dirname = file.getParent().getFileName().toString();
            if (dirname.equals("scratch") || dirname.equals("state") || dirname.equals("action") || dirname.equals("actions-done")) {
                continue;
            }

            log.debug("Artifact {}", filename);
            if (filename.endsWith(".warc.gz")) {
                warcs.add(file);
            } else {
                artifacts.add(new Artifact(relpath.toString(), file));
            }
        }

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
    public String version() {
        try {
            return "Heritrix version " + heritrix.getEngine().heritrixVersion;
        } catch (IOException e) {
            log.warn("Unable to get Heritrix version", e);
            return "Heritrix version unknown";
        }
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
