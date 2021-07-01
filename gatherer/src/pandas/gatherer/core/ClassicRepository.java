package pandas.gatherer.core;

import org.springframework.stereotype.Component;
import pandas.gather.Instance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Component
public class ClassicRepository implements Repository {
    private final Config config;

    public ClassicRepository(Config config) {
        this.config = config;
    }

    @Override
    public void storeArtifacts(Instance instance, List<Path> files) throws IOException {
        for (Path src: files) {
            Path destDir;
            String fileName = src.getFileName().toString();
            if (fileName.startsWith("ps-")) {
                destDir = getMasterDir(instance.getPi(), "preserve/arc3");
            } else if (fileName.startsWith("ac-")) {
                destDir = getMasterDir(instance.getPi(), "access/arc3");
            } else if (fileName.startsWith("mi-")) {
                destDir = getMasterDir(instance.getPi(), "mime/arc3");
            } else {
                throw new IllegalArgumentException("don't know where to store: " + src.getFileName());
            }
            if (!Files.exists(destDir)) Files.createDirectories(destDir);
            Path tmp = destDir.resolve(src.getFileName() + ".tmp");
            Path dest = destDir.resolve(src.getFileName());
            Files.copy(src, tmp, REPLACE_EXISTING);
            Files.move(tmp, dest, REPLACE_EXISTING);
        }
    }

    @Override
    public void storeWarcs(Instance instance, List<Path> warcs) throws IOException {
        long pi = instance.getPi();
        for (Path warcGz : warcs) {
            if (config.getRepo2Dir() != null) {
                Path repo2Dir = config.getRepo2Dir().resolve(String.format("%03d", pi / 1000)).resolve(Long.toString(pi));
                if (!Files.exists(repo2Dir)) Files.createDirectories(repo2Dir);
                Files.copy(warcGz, repo2Dir.resolve(warcGz.getFileName()));
            }

            Path repo1Dir = config.getRepo1Dir().resolve(String.format("%03d", pi / 1000)).resolve(Long.toString(pi));
            if (!Files.exists(repo1Dir)) Files.createDirectories(repo1Dir);
            Files.move(warcGz, repo1Dir.resolve(warcGz.getFileName()));
        }
    }

    private Path getMasterDir(long pi, String s) {
        String piGroup = String.format("%03d", pi / 1000);
        return config.getMastersDir().resolve(s).resolve(piGroup).resolve(Long.toString(pi));
    }
}
