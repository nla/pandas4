package pandas.gatherer.core;

import pandas.gather.Instance;
import pandas.gatherer.heritrix.BambooClient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class BambooRepository implements Repository {
    private final BambooClient bambooClient;

    public BambooRepository(BambooClient bambooClient) {
        this.bambooClient = bambooClient;
    }

    @Override
    public void storeArtifacts(Instance instance, List<Artifact> artifacts) throws IOException {
        long crawlId = getOrCreateCrawl(instance);
        for (Artifact artifact: artifacts) {
            bambooClient.putArtifactIfNotExists(crawlId, artifact.path(), artifact.file());
        }
    }

    private long getOrCreateCrawl(Instance instance) {
        String crawlName = instance.getTitle().getName() + " [" + instance.getHumanId() + "]";
        long crawlId = bambooClient.getOrCreateCrawl(crawlName, instance.getId());
        return crawlId;
    }

    @Override
    public void storeWarcs(Instance instance, List<Path> warcs) throws IOException {
        long crawlId = getOrCreateCrawl(instance);
        for (Path warc: warcs) {
            bambooClient.putWarcIfNotExists(crawlId, warc.getFileName().toString(), warc);
        }
    }
}
