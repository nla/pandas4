package pandas.gatherer.core;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileStats {
    private long fileCount = 0;
    private long size = 0;

    public FileStats(long fileCount, long size) {
        this.fileCount = fileCount;
        this.size = size;
    }

    FileStats(Path path, StopCondition stopCondition) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (stopCondition.shouldStop()) {
                    return FileVisitResult.TERMINATE;
                }
                if (dir.getFileName().toString().startsWith("hts-")) {
                    return FileVisitResult.SKIP_SUBTREE;
                } else {
                    return FileVisitResult.CONTINUE;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (stopCondition.shouldStop()) {
                    return FileVisitResult.TERMINATE;
                }
                if (!file.getFileName().toString().startsWith("hts-")) {
                    fileCount++;
                    size += attrs.size();
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
    }

    public long fileCount() {
        return fileCount;
    }

    public long size() {
        return size;
    }
}
