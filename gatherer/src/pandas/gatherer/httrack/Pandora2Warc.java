package pandas.gatherer.httrack;

import org.apache.commons.codec.binary.Base32;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Pandora2Warc {
    private static final Pattern PANACCESS = Pattern.compile("\\.panaccess.*");
    private static final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        dateFmt.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
    }

    private static class WarcWriter implements Closeable {
        private OutputStream stream;
        private String currentPath;
        private final String pathPattern;
        private final long maxLength = 1024l * 1024 * 1024;
        private final byte[] trailer = "\r\n\r\n".getBytes(UTF_8);
        int serial = 0;
        private List<Path> writtenFiles = new ArrayList<>();

        public WarcWriter(String pathPattern) throws IOException {
            this.pathPattern = pathPattern;
            openNextFile();
        }

        private void openNextFile() throws IOException {
            closeCurrentFile();
            this.currentPath = String.format(pathPattern, serial);
            this.stream = Files.newOutputStream(Paths.get(currentPath + ".open"));
            serial++;
        }

        private void closeCurrentFile() throws IOException {
            if (stream == null) return;
            stream.close();
            Path path = Paths.get(currentPath);
            Files.move(Paths.get(currentPath + ".open"), path, StandardCopyOption.REPLACE_EXISTING);
            writtenFiles.add(path);
            currentPath = null;
            stream = null;
        }

        @Override
        public void close() throws IOException {
            closeCurrentFile();
        }

        public void write(String header, Path body) throws IOException {
            if (Files.size(Paths.get(currentPath + ".open")) >= maxLength) {
                openNextFile();
            }
            GZIPOutputStream gz = new GZIPOutputStream(stream);
            byte[] headerBytes = header.getBytes(UTF_8);
            gz.write(headerBytes);
            Files.copy(body, gz);
            gz.write(trailer);
            gz.finish();
        }
    }

    private static String b32sha1(MessageDigest md) {
        return "sha1:" + new Base32().encodeToString(md.digest()).toUpperCase(Locale.ROOT);
    }

    private static String sha1File(Path f) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        byte[] buffer = new byte[1024 * 1024];
        try (InputStream is = Files.newInputStream(f)) {
            while (true) {
                int length = is.read(buffer);
                if (length < 0) break;
                md.update(buffer, 0, length);
            }
        }
        return b32sha1(md);
    }

    private static String makeHeader(Path file, Date cdate, int prefixSize, Map<String, String> typeMap) throws IOException, NoSuchAlgorithmException {
        String relpath = file.toString().substring(prefixSize);
        String encodedPath = URLEncoder.encode(relpath, UTF_8).replace("%2F", "/");
        String contentType = typeMap.get(relpath);
        if (contentType == null) contentType = HttrackUtils.mimeTypeByExtension(relpath);
        if (contentType == null) contentType = "application/octet-stream";
        return "WARC/1.0\r\n" +
        "WARC-Type: resource\r\n" +
        "WARC-Target-URI: http://pandora.nla.gov.au/pan/" + encodedPath + "\r\n" +
        "WARC-Record-ID: <urn:uuid:" + UUID.randomUUID() + ">\r\n" +
        "WARC-Date: "  + dateFmt.format (cdate) + "\r\n" +
        "WARC-Block-Digest: " + sha1File(file) + "\r\n" +
        "Content-Type: " + contentType + "\r\n" +
        "Content-Length: " + Files.size(file) + "\r\n" +
        "\r\n";
    }

    private static void writeRecord(Path file, Date ctime, int prefixSize, Map<String, String> typeMap, WarcWriter warc) {
        try {
            warc.write(makeHeader(file, ctime, prefixSize, typeMap), file);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Parse hts-cache/new.zip and build a map from filename -> content-type.
     */
    private static Map<String, String> buildTypeMap(Path dir) throws IOException {
        Path htsZip = dir.resolve("hts-cache").resolve("new.zip");
        if (!Files.exists(htsZip)) return Collections.emptyMap();
        var map = new HashMap<String,String>();
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(htsZip))) {
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                Map<String,String> extra = HttrackUtils.parseExtra(entry);
                String xsave = extra.get("X-Save");
                String mime = extra.get("Content-Type");
                map.put(xsave, mime);
            }
        }
        return map;
    }

    public static List<Path> convertInstance(Path srcDir, Path destDir) throws IOException {
        String pi = srcDir.getParent().getFileName().toString();
        String timestamp = srcDir.getFileName().toString();
        int prefixSize = srcDir.getParent().getParent().toString().length() + 1;
        String outpattern = destDir.resolve("nla.arc-" + pi + "-" + timestamp + "-%03d.warc.gz").toString();
        var typeMap = buildTypeMap(srcDir);

        WarcWriter warcWriter = new WarcWriter(outpattern);
        try (WarcWriter warc = warcWriter) {
            Files.walkFileTree(srcDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    Date ctime = new Date(attrs.creationTime().toMillis());
                    if (Files.exists(path) && !Files.isDirectory(path) && !PANACCESS.matcher(path.getFileName().toString()).matches()) {
                        writeRecord(path, ctime, prefixSize, typeMap, warc);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        return warcWriter.writtenFiles;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: Pandora2Warc src-dir dest-dir");
            System.exit(1);
        }
        convertInstance(Paths.get(args[0]), Paths.get(args[1]));
    }
}
