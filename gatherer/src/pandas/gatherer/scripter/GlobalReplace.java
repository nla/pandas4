package pandas.gatherer.scripter;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Global find and replace utility.
 *
 * Based on the old globrep.pl script.
 */
public class GlobalReplace {
    public static class Report {
        public long directoriesProcessed;
        public long substitutionsMade;
        public long filesChanged;
        public long filesProcessed;
        public Instant startTime;
        public Instant endTime;

        public String toString() {
            return "----------------------------------------------------\n" +
                    "Directories processed: " + directoriesProcessed + "\n" +
                    "Files processed: " + filesProcessed + "\n" +
                    "Files changed: " + filesChanged + "\n" +
                    "Substitutions made: " + substitutionsMade + "\n" +
                    "Start time: " + startTime.atZone(ZoneId.systemDefault()) + "\n" +
                    "End time: " + endTime.atZone(ZoneId.systemDefault()) + "\n" +
                    "Elapsed time: " + Duration.between(startTime, endTime).toSeconds() + " seconds\n" +
                    "----------------------------------------------------";
        }
    }

    /**
     * Walks a directory tree and recursively applying regex replacements. Uses iso-8859-1 as the charset to avoid
     * corrupting files that aren't unicode. Files larger than 20MB are ignored for implementation simplicity.
     */
    public static Report globrep(Path root, int maxDepth, String filenameGlob, Pattern needle, String replacement,
                                 Appendable log) throws IOException {
        PathMatcher filenameMatcher = FileSystems.getDefault().getPathMatcher("glob:**/" + filenameGlob);
        int sizeLimit = 20 * 1024 * 1024;
        Report report = new Report();
        report.startTime = Instant.now();
        Files.walkFileTree(root, Set.of(), maxDepth, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (!Files.isRegularFile(path)) return CONTINUE;
                if (!filenameMatcher.matches(path)) return CONTINUE;
                if (Files.size(path) > sizeLimit) return CONTINUE;

                String contents = Files.readString(path, ISO_8859_1);

                Matcher m = needle.matcher(contents);
                int matches = 0;
                StringBuffer sb = new StringBuffer();
                while (m.find()) {
                    matches++;
                    if (replacement != null) m.appendReplacement(sb, replacement);
                }

                if (matches > 0) {
                    if (replacement != null) {
                        m.appendTail(sb);
                        Path tmp = Paths.get(path + ".tmppandas");
                        Files.writeString(tmp, sb.toString(), ISO_8859_1);
                        Files.move(tmp, path, REPLACE_EXISTING);
                        if (log != null) log.append(root.relativize(path).toString()).append(" matched with ").append(String.valueOf(matches)).append(" changes\n");
                    } else {
                        if (log != null) log.append(root.relativize(path).toString()).append(" matched ").append(String.valueOf(matches)).append(" times\n");
                    }

                    report.filesChanged++;
                    report.substitutionsMade += matches;
                }

                report.filesProcessed++;
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                report.directoriesProcessed++;
                return super.postVisitDirectory(dir, exc);
            }
        });
        report.endTime = Instant.now();
        return report;
    }
}
