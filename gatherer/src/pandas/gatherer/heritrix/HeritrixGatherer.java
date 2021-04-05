package pandas.gatherer.heritrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pandas.gather.GatherMethod;
import pandas.gather.Instance;
import pandas.gather.InstanceService;
import pandas.gather.State;
import pandas.gatherer.core.Backend;
import pandas.gatherer.core.Config;
import pandas.gatherer.core.WorkingArea;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    private final BambooClient bamboo;
    private boolean shutdown;

    public HeritrixGatherer(Config config, HeritrixConfig heritrixConfig, WorkingArea workingArea, InstanceService instanceService, BambooClient bamboo) {
        this.workingArea = workingArea;
        this.config = config;
        this.heritrixConfig = heritrixConfig;
        heritrix = new HeritrixClient(heritrixConfig.getUrl(), heritrixConfig.getUser(), heritrixConfig.getPassword());
        this.instanceService = instanceService;
        this.bamboo = bamboo;
    }

    @Override
    public void gather(Instance instance) throws Exception {
        // create heritrix dirs
        Path jobDir = jobDir(instance);
        Files.createDirectories(jobDir);
        CrawlBeans.writeConfig(config, instance, jobDir);

        // create pywb dirs
        Path collDir = pywbDir(instance);
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

        pywbReindex(instance);
    }

    private static void createSymbolicLinkIfNotExists(Path link, Path target) throws IOException {
        if (!Files.exists(link)) {
            Files.createSymbolicLink(link, target);
        }
    }

    @Override
    public void postprocess(Instance instance) throws IOException {
        // does nothing for heritrix crawls
    }

    public void pywbReindex(Instance instance) throws IOException {
        String wbManager = config.getPywbDir().resolve("bin/wb-manager").toString();
        try {
            new ProcessBuilder(wbManager, "reindex", pywbColl(instance))
                    .directory(config.getPywbDataDir().toFile())
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (InterruptedException e) {
            log.warn("Interrupted indexing", e);
        }
    }

    private Path pywbDir(Instance instance) {
        return config.getPywbDataDir().resolve("collections").resolve(pywbColl(instance));
    }

    private String pywbColl(Instance instance) {
        return instance.getTitle().getPi() + "-" + instance.getDateString();
    }

    @Override
    public void archive(Instance instance) throws IOException {
        if (config.getBambooSeriesId() == null) {
            throw new IllegalStateException("BAMBOO_SERIES_ID is not configured");
        }

        Path jobDir = jobDir(instance);

        String crawlName = instance.getTitle().getName() + " [" + instance.getHumanId() + "]";

        long crawlId = bamboo.getOrCreateCrawl(crawlName, instance.getId());



//        for (Path file : Files.walk(jobDir).collect(toList())) {
//            Path path = jobDir.relativize(file);
//            ArtifactType type = artifactType(path);
//            if (type == null) {
//                continue;
//            }
//
//            if (type == ArtifactType.WARC) {
////                warcs.add(new Warc(BambooDB.WARC_STATE_IMPORTED, file.getFileName().toString(),
////                        blob.id(), blob.size(), sha256));
//            } else {
////                Instant lastModified = Files.getLastModifiedTime(file).toInstant();
////                artifacts.add(new Artifact(blob.id(), blob.size(), sha256, null, path.toString(),
////                        lastModified, type));
//            }
//            log.debug("{} {} blobId={} sha256={}", type, path, blob.id(), sha256);
//        }

        delete(instance);
    }

//    static ArtifactType artifactType(Path path) {
//        String filename = path.getFileName().toString();
//        if (filename.endsWith(".lck")) {
//            return null;
//        } else if (filename.endsWith(".log")) {
//            return ArtifactType.LOG;
//        } else if (filename.endsWith(".cxml")) {
//            return ArtifactType.CONFIG;
//        } else if (filename.endsWith(".recover.gz")) {
//            return ArtifactType.RECOVER;
//        } else if (filename.endsWith(".dump")) {
//            return ArtifactType.DUMP;
//        } else if (Files.isDirectory(path)) {
//            return null;
//        }
//        Path parent = path.getParent();
//        if (parent != null) {
//            switch (parent.getFileName().toString()) {
//                case "reports":
//                    return ArtifactType.REPORT;
//                case "logs":
//                    return ArtifactType.LOG;
//                case "warcs":
//                    return ArtifactType.WARC;
//                case "scratch":
//                case "state":
//                case "action":
//                case "actions-done":
//                default:
//                    return null;
//            }
//        }
//        return null;
//    }

    @Override
    public void delete(Instance instance) throws IOException {
        workingArea.deleteInstance(instance.getTitle().getPi(), instance.getDateString());
        workingArea.deleteRecursivelyIfExists(pywbDir(instance));
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
