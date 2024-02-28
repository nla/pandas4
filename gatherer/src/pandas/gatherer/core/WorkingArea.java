package pandas.gatherer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pandas.gather.Instance;
import pandas.gatherer.httrack.Pandora2Warc;
import pandas.gatherer.repository.Repository;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.ProcessBuilder.Redirect.INHERIT;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Component
public class WorkingArea {
    private static final Logger log = LoggerFactory.getLogger(WorkingArea.class);
    private final Path workingdir;

    private final static Set<PosixFilePermission> DIR_PERMS = new HashSet<>(Arrays.asList(
            PosixFilePermission.OWNER_EXECUTE,
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.GROUP_EXECUTE,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.GROUP_WRITE,
            PosixFilePermission.OTHERS_EXECUTE,
            PosixFilePermission.OTHERS_READ
    ));
    private final Config config;
    private final Repository repository;

    public WorkingArea(Config config, Repository repository) {
        this.config = config;
        this.workingdir = config.getWorkingDir();
        this.repository = repository;
    }

    private Path instancePath(long pi, String date) {
        return workingdir.resolve(Long.toString(pi)).resolve(date);
    }

    public void createInstance(long pi, String date) throws IOException {
        Files.createDirectories(instancePath(pi, date));
    }

    /**
     * Create pre-qa preservation tarball metadata files.
     */
    public void preserveInstance(Instance instance) throws IOException {
        long pi = instance.getPi();
        String date = instance.getDateString();
        Path uploadDir = config.getUploadDir().toAbsolutePath();
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

        // create preservation tarball and metadata files
        Path pwd = instancePath(pi, date);
        Path tgz = uploadDir.resolve("ps-ar2-" + pi + "-" + date + ".tgz");
        Path lst = uploadDir.resolve("ps-ar2-" + pi + "-" + date + ".lst");
        Path sz = uploadDir.resolve("ps-ar2-" + pi + "-" + date + ".sz");
        Path md5 = uploadDir.resolve("ps-ar2-" + pi + "-" + date + ".md5");

        try {
            fixDirectoryPermsRecursively(pwd);
        } catch (IOException e) {
            log.warn("Error recursively fixing directory permissions under {}", pwd, e);
        }

        exec(workingdir, "tar", "-zcf", tgz.toString(), pi + "/" + date);
        execRedir(workingdir, lst,"find", pi + "/" + date, "-type", "f", "-exec", "ls", "-l", "{}", ";");
        execRedir(workingdir, sz,"du", "-c", pi + "/" + date);
        execRedir(pwd, md5, "md5sum", tgz.toString());

        repository.storeArtifactPaths(instance, Arrays.asList(tgz, lst, sz, md5));

        Files.deleteIfExists(tgz);
        Files.deleteIfExists(lst);
        Files.deleteIfExists(sz);
        Files.deleteIfExists(md5);
    }

    /**
     * Move in two steps with so never have a partially written file in the final location.
     */
    public void twoStepMove(Path src, Path destDir) throws IOException {
        log.info("Moving {} to {}", src, destDir);
        Path tmp = destDir.resolve(src.getFileName() + ".tmp");
        Path dest = destDir.resolve(src.getFileName());
        Files.move(src, tmp, REPLACE_EXISTING);
        Files.move(tmp, dest, REPLACE_EXISTING);
    }

    public void archiveInstance(Instance instance) throws IOException {
        long pi = instance.getPi();
        String date = instance.getDateString();

        Path uploadDir = config.getUploadDir().toAbsolutePath();
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

        // create access tarball and metadata files
        Path pwd = instancePath(pi, date);
        Path tgz = uploadDir.resolve("ac-ar2-" + pi + "-" + date + ".tgz");
        Path lst = uploadDir.resolve("ac-ar2-" + pi + "-" + date + ".lst");
        Path sz = uploadDir.resolve("ac-ar2-" + pi + "-" + date + ".sz");
        Path md5 = uploadDir.resolve("ac-ar2-" + pi + "-" + date + ".md5");
        String relpath = pi + "/" + date;
        exec(workingdir, "chmod", "-R", "gu=rwX,o=rX", relpath);
        exec(workingdir, "tar", "-zcf", tgz.toString(), relpath);
        execRedir(workingdir, lst,"find", relpath, "-type", "f", "-exec", "ls", "-l", "{}", ";");
        execRedir(workingdir, sz,"du", "-c", relpath);
        execRedir(workingdir, md5, "md5sum", tgz.toString());

        // create mime tarball
        Path mimeTgz = uploadDir.resolve("mi-ar2-" + pi + "-" + date + ".tgz");
        Path insMime = config.getMimeDir().resolve(Long.toString(pi)).resolve(date);
        if (!Files.exists(insMime)) Files.createDirectories(insMime);
        exec(config.getMimeDir(), "tar", "-zcf", mimeTgz.toString(), relpath);

        // construct warc
        List<Path> warcs = Pandora2Warc.convertInstance(pwd, uploadDir);
        repository.storeWarcs(instance, warcs);

        // copy to master storage
        repository.storeArtifactPaths(instance, Arrays.asList(tgz, lst, sz, md5, mimeTgz));

        for (Path warc : warcs) {
            Files.deleteIfExists(warc);
        }
        Files.deleteIfExists(tgz);
        Files.deleteIfExists(lst);
        Files.deleteIfExists(sz);
        Files.deleteIfExists(md5);
        Files.deleteIfExists(mimeTgz);
        deleteInstance(instance.getPi(), instance.getDateString());
    }

    private void execRedir(Path pwd, Path stdout, String... args) throws IOException {
        try {
            int status = new ProcessBuilder(args)
                    .directory(pwd.toFile())
                    .redirectError(INHERIT)
                    .redirectOutput(stdout.toFile())
                    .start()
                    .waitFor();
            if (status != 0) throw new IOException("Non-zero status code: " + String.join(" ", Arrays.asList(args)));
        } catch (InterruptedException e) {
            throw new IOException("Interrupted", e);
        }
    }

    private void exec(Path pwd, String... args) throws IOException {
        try {
            int status = new ProcessBuilder(args)
                    .directory(pwd.toFile())
                    .redirectError(INHERIT)
                    .redirectOutput(INHERIT)
                    .start()
                    .waitFor();
            if (status != 0) throw new IOException("Non-zero status code: " + String.join(" ", Arrays.asList(args)));
        } catch (InterruptedException e) {
            throw new IOException("Interrupted", e);
        }
    }

    public void deleteInstance(long pi, String date) throws IOException {
        deleteRecursivelyIfExists(instancePath(pi, date));
        deleteRecursivelyIfExists(mimePath(pi, date));

        // remove the title directory if it's empty
        try {
            Files.deleteIfExists(workingdir.resolve(Long.toString(pi)));
        } catch (DirectoryNotEmptyException e) {
            // that's ok.
        }
    }

    public FileStats instanceStats(long pi, String date, StopCondition stopCondition) throws IOException {
        return new FileStats(instancePath(pi, date), stopCondition);
    }

    private Path mimePath(long pi, String date) {
        return workingdir.resolve("mime").resolve(Long.toString(pi)).resolve(date);
    }

    private void fixDirectoryPermsRecursively(Path path) throws IOException {
        // we can't use walkFileTree for this because walkFileTree attempts
        // to read the directory before calling preVisit
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            Files.setPosixFilePermissions(path, DIR_PERMS);
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
                for (Path entry : paths) {
                    fixDirectoryPermsRecursively(entry);
                }
            }
        }
    }

    public void deleteRecursivelyIfExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        try {
            fixDirectoryPermsRecursively(path);
        } catch (IOException e) {
            log.warn("Unable to fix permissions: " + path, e);
        }

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                log.warn("Error visiting {}", file, exc);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public Path getInstanceDir(long pi, String dateString) {
        return workingdir.resolve(String.valueOf(pi)).resolve(dateString).toAbsolutePath();
    }
}
