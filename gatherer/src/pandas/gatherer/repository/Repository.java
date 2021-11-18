package pandas.gatherer.repository;

import pandas.gather.Instance;
import pandas.gatherer.core.Artifact;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface Repository {
    default void storeArtifactPaths(Instance instance, List<Path> files) throws IOException {
        storeArtifacts(instance, files.stream()
                .map(path -> new Artifact(path.getFileName().toString(), path))
                .toList());
    }

    void storeArtifacts(Instance instance, List<Artifact> files) throws IOException;

    void storeWarcs(Instance instance, List<Path> warcs) throws IOException;
}
