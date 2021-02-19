package pandas.gatherer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
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

    public WorkingArea(Config config) {
        this.config = config;
        this.workingdir = config.getWorkingDir();
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
    public void preserveInstance(long pi, String date) throws IOException {
        // create preservation tarball and metadata files
        Path pwd = instancePath(pi, date);
        Path tgz = config.getUploadDir().resolve("ps-ar2-" + pi + "-" + date + ".tgz");
        Path lst = config.getUploadDir().resolve("ps-ar2-" + pi + "-" + date + ".lst");
        Path sz = config.getUploadDir().resolve("ps-ar2-" + pi + "-" + date + ".sz");
        Path md5 = config.getUploadDir().resolve("ps-ar2-" + pi + "-" + date + ".md5");

        exec(workingdir, "tar", "-zcf", tgz.toString(), pi + "/" + date);
        execRedir(workingdir, lst,"find", pi + "/" + date, "-type", "f", "-exec", "ls", "-l", "{}", ";");
        execRedir(workingdir, sz,"du", "-c", pi + "/" + date);
        execRedir(pwd, md5, "md5sum", tgz.toString());

        Path destDir = getMasterDir(pi, "preserve/arc3");
        if (!Files.exists(destDir)) Files.createDirectories(destDir);
        twoStepMove(tgz, destDir);
        twoStepMove(lst, destDir);
        twoStepMove(sz, destDir);
        twoStepMove(md5, destDir);
    }

    private Path getMasterDir(long pi, String s) {
        String piGroup = String.format("%03d", pi / 1000);
        return config.getMastersDir().resolve(s).resolve(piGroup).resolve(Long.toString(pi));
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

    public void archiveInstance(long pi, String date) throws IOException {
        // create access tarball and metadata files
        Path pwd = instancePath(pi, date);
        Path tgz = config.getUploadDir().resolve("ac-ar2-" + pi + "-" + date + ".tgz");
        Path lst = config.getUploadDir().resolve("ac-ar2-" + pi + "-" + date + ".lst");
        Path sz = config.getUploadDir().resolve("ac-ar2-" + pi + "-" + date + ".sz");
        Path md5 = config.getUploadDir().resolve("ac-ar2-" + pi + "-" + date + ".md5");
        exec(workingdir, "chmod", "-R", "gu=rwX,o=rX", pwd.toString());
        exec(workingdir, "tar", "-zcf", tgz.toString(), pi + "/" + date);
        execRedir(workingdir, lst,"find", pi + "/" + date, "-type", "f", "-exec", "ls", "-l", "{}", ";");
        execRedir(workingdir, sz,"du", "-c", pi + "/" + date);
        execRedir(workingdir, md5, "md5sum", tgz.toString());

        // create mime tarball
        Path mimeTgz = config.getUploadDir().resolve("mi-ar2-" + pi + "-" + date + ".tgz");
        Path insMime = config.getMimeDir().resolve(Long.toString(pi)).resolve(date);
        if (!Files.exists(insMime)) Files.createDirectories(insMime);
        exec(config.getMimeDir(), "tar", "-zcf", mimeTgz.toString(), pi + "/" + date);

        // construct warc
        if (config.getLegacyScripts() != null) {
            ProcessBuilder pb = new ProcessBuilder("java", "-Xmx64m", "-jar",
                    config.getLegacyScripts().resolve("pandora2warc.jar").toString(),
                    config.getUploadDir().toString(), workingdir.toString());
            pb.environment().put("LC_ALL", "C");
            int status;
            try {
                status = pb.start().waitFor();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            if (status != 0) throw new IOException("pandora2warc returned code " + status);

            // compress warc
            exec(workingdir, "java", "-Xmx64m", "-cp",
                    config.getLegacyScripts().resolve("warcserve.jar").toString(),
                    "warcserve.gzip_warc", "1048576000",
                    config.getUploadDir().resolve("nla.arc-" + pi + "-" + date + ".warc").toString());

            for (int i = 0;; i++) {
                Path warcGz = config.getUploadDir().resolve("nla.arc-" + pi + "-" + date + "-" + String.format("%03d", i) + ".warc.gz");
                if (!Files.exists(warcGz)) break;

                if (config.getRepo2Dir() != null) {
                    Path repo2Dir = config.getRepo2Dir().resolve(String.format("%03d", pi / 1000)).resolve(Long.toString(pi));
                    if (!Files.exists(repo2Dir)) Files.createDirectories(repo2Dir);
                    log.info("Copying {} to {}", warcGz, repo2Dir);
                    Files.copy(warcGz, repo2Dir);
                }

                Path repo1Dir = config.getRepo1Dir().resolve(String.format("%03d", pi / 1000)).resolve(Long.toString(pi));
                if (!Files.exists(repo1Dir)) Files.createDirectories(repo1Dir);
                log.info("Moving {} to {}", warcGz, repo1Dir);
                Files.move(warcGz, repo1Dir);
            }
        }

        // copy to master storage
        Path destDir = getMasterDir(pi, "access/arc3");
        if (!Files.exists(destDir)) Files.createDirectories(destDir);
        twoStepMove(tgz, destDir);
        twoStepMove(lst, destDir);
        twoStepMove(sz, destDir);
        twoStepMove(md5, destDir);
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
        });
    }

    public Path getInstanceDir(long pi, String dateString) {
        return workingdir.resolve(String.valueOf(pi)).resolve(dateString);
    }
}
