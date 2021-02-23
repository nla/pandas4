package pandas.gatherer.httrack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardOpenOption.*;

public class HttrackUtils {
    static Map<String, String> mimeTypes = parseMimeTypes();

    private static Map<String, String> parseMimeTypes() {
        Map<String, String> map = new HashMap<>();
        InputStream stream = HttrackUtils.class.getResourceAsStream("mime.types");
        if (stream == null) throw new RuntimeException("Missing resource mime.types");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.replaceFirst("#.*", "").strip();
                if (line.isBlank()) continue;
                String[] parts = line.split("\\s+");
                for (int i = 1; i < parts.length; i++) {
                    map.put(parts[i], parts[0]);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return map;
    }

    public static String mimeTypeByExtension(String filename) {
        String extension = filename.replaceFirst(".*\\.", "");
        return mimeTypes.get(extension);
    }

    public static void postGather(long pi, String date, Path root) throws IOException {
        writeUrlAndMimeMappings(pi, date, root);
        Files.deleteIfExists(root.resolve("external.html"));
        Files.deleteIfExists(root.resolve("fade.gif"));
        Files.deleteIfExists(root.resolve("backblue.gif"));
    }

    private static void writeUrlAndMimeMappings(long pi, String date, Path root) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(root.resolve("hts-cache").resolve("new.zip")));
             BufferedWriter urlMapWriter = Files.newBufferedWriter(root.resolve("url.map"), TRUNCATE_EXISTING, CREATE, WRITE)) {
            while (true) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) break;

                String url = entry.getName();
                var extra = parseExtra(entry);

                String saveFile = extra.get("X-Save");
                String qualSaveFile = pi + "/" + date + "/" + saveFile;
                if (saveFile == null) continue;

                urlMapWriter.append(url).append("^^").append(qualSaveFile);

                String mime = extra.get("Content-Type");
                if (mime == null || mime.isBlank() || mime.contains(" ")) continue;

                if (mimeTypeByExtension(saveFile).equals(mime)) continue;

                Path savePath = root.resolve(saveFile);
                Path saveDir = savePath.getParent();
                if (!Files.isDirectory(saveDir)) continue;

                Path panaccess = saveDir.resolve(".panaccess-mime.types");
                try (BufferedWriter writer = Files.newBufferedWriter(panaccess, APPEND, CREATE, WRITE)) {
                    writer.append("<Files \"").append(String.valueOf(savePath.getFileName()));
                    writer.append("\">\n\tForceType ").append(mime).append("\n</Files>\n\n");
                }
            }
        }
    }

    static Map<String, String> parseExtra(ZipEntry entry) {
        byte[] extra = entry.getExtra();
        if (extra == null) return Collections.emptyMap();
        return parseExtra(new String(extra, StandardCharsets.ISO_8859_1));
    }

    static Map<String, String> parseExtra(String extra) {
        Map<String, String> map = new HashMap<>();
        String[] lines = extra.split("\r\n");
        for (int i = 1; i < lines.length; i++) {
            String[] parts = lines[i].split(":\\s*", 2);
            map.put(parts[0], parts[1]);
        }
        return map;
    }
}
