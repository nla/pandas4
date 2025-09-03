package pandas.gatherer.core;

import pandas.gather.Instance;

import java.io.IOException;

public interface Backend {
    String getGatherMethod();

    int gather(Instance instance) throws Exception;

    void postprocess(Instance instance) throws IOException, InterruptedException;

    void archive(Instance instance) throws IOException, InterruptedException;

    void delete(Instance instance) throws IOException;

    String version() throws IOException;

    default void shutdown() {
    }

    int getWorkerCount();
}
