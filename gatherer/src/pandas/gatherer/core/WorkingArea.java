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

    public WorkingArea(Config config) {
        this.workingdir = config.getWorkingDir();
    }

    private Path instancePath(long pi, String date) {
        return workingdir.resolve(Long.toString(pi)).resolve(date);
    }

    public void createInstance(long pi, String date) throws IOException {
        Files.createDirectories(instancePath(pi, date));
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
