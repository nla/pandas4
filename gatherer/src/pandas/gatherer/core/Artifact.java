package pandas.gatherer.core;

import java.nio.file.Path;

public class Artifact {
    private final String path;
    private final Path file;

    public Artifact(String path, Path file) {
        this.path = path;
        this.file = file;
    }

    public String path() {
        return path;
    }

    public Path file() {
        return file;
    }
}
