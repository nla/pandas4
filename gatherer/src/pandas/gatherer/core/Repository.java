package pandas.gatherer.core;

import pandas.gather.Instance;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface Repository {
    void storeArtifacts(Instance instance, List<Path> files) throws IOException;

    void storeWarcs(Instance instance, List<Path> warcs) throws IOException;
}
