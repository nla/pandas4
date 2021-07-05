package pandas.gatherer.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import pandas.gather.Instance;
import pandas.gatherer.core.Artifact;
import pandas.gatherer.core.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Component
@ConditionalOnMissingBean(BambooRepository.class)
public class ClassicRepository implements Repository {
    private final Logger log = LoggerFactory.getLogger(ClassicRepository.class);
    private final Config config;

    public ClassicRepository(Config config) {
        this.config = config;
    }

    @Override
    public void storeArtifacts(Instance instance, List<Artifact> artifacts) throws IOException {
        for (Artifact artifact: artifacts) {
            String fileName = artifact.path().replaceAll(".*/", "");
            Path destDir;
            if (fileName.startsWith("ps-")) {
                destDir = getMasterDir(instance.getPi(), "preserve/arc3");
            } else if (fileName.startsWith("ac-")) {
                destDir = getMasterDir(instance.getPi(), "access/arc3");
            } else if (fileName.startsWith("mi-")) {
                destDir = getMasterDir(instance.getPi(), "mime/arc3");
            } else {
                log.warn("classic crawl bundles not implemented ({})", fileName);
                continue;
            }
            Path tmp = destDir.resolve(artifact.path() + ".tmp");
            Path dest = destDir.resolve(artifact.path());
            if (!Files.exists(dest.getParent())) Files.createDirectories(dest.getParent());
            Files.copy(artifact.file(), tmp, REPLACE_EXISTING);
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
