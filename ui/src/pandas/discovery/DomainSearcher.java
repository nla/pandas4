package pandas.discovery;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pandas.core.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Search for domains in the Common Crawl domain ranks file.
 */
@Service
public class DomainSearcher {
    private static final Logger log = LoggerFactory.getLogger(DomainSearcher.class);
    private final Path dataDir;
    private final Path domainsListFile;
    private boolean noGrep;
    private Thread downloadThread;
    private boolean failed;

    public DomainSearcher(Config config) {
        this.dataDir = config.getDataPath().resolve("unearth");
        this.domainsListFile = dataDir.resolve("domains.txt");
    }

    public void downloadAndLoadDomainRanks() throws IOException {
        var url = "https://data.commoncrawl.org/projects/hyperlinkgraph/cc-main-2023-may-sep-nov/domain/cc-main-2023-may-sep-nov-domain-ranks.txt.gz";
        var ccDomainRanksFile = dataDir.resolve("cc-main-2023-may-sep-nov-domain-ranks.txt.gz");
        if (!Files.exists(domainsListFile)) {
            Files.createDirectories(dataDir);
            if (!Files.exists(ccDomainRanksFile)) {
                download(url, ccDomainRanksFile);
            }
            buildDomainsList(ccDomainRanksFile, domainsListFile);
        }
    }

    private void buildDomainsList(Path ccDomainRanksFile, Path domainsListFile) throws IOException {
        log.info("Building domain list from {}", ccDomainRanksFile);
        Path tmp = Paths.get(domainsListFile + ".tmp");
        try (var reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                Files.newInputStream(ccDomainRanksFile), 8192), US_ASCII));
             var writer = Files.newBufferedWriter(tmp, US_ASCII)) {
            String header = reader.readLine();
            if (!header.startsWith("#harmonicc_pos")) {
                throw new IOException("Unexpected header line in " + ccDomainRanksFile + ": " + header.substring(0, 100));
            }
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                // #harmonicc_pos #harmonicc_val #pr_pos #pr_val #host_rev #n_hosts
                String[] parts = line.split("\t");
                String revDomain = parts[4];
                String domain = reverseDomain(revDomain);
                writer.write(domain);
                writer.write('\n');
            }
        }
        Files.move(tmp, domainsListFile, REPLACE_EXISTING);
    }

    /**
     * "a.b.c" -> "c.b.a"
     */
    @NotNull
    private static String reverseDomain(String revDomain) {
        String[] segments = revDomain.split("\\.");
        Collections.reverse(Arrays.asList(segments));
        return String.join(".", segments);
    }

    public void download(String url, Path dest) throws IOException {
        log.info("Downloading {} to {}", url, dest);
        Path tmp = Paths.get(dest + ".tmp");
        try (var in = URI.create(url).toURL().openStream()) {
            Files.copy(in, tmp);
            Files.move(tmp, dest, REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    public List<String> searchDomains(String pattern, int limit) throws IOException {
        ensureReady();
        if (!noGrep) {
            try {
                return searchDomainsGrep(pattern, limit);
            } catch (IOException e) {
                log.warn("grep failed, falling back to Java", e);
                noGrep = true;
            }
        }
        return searchDomainsJava(pattern, limit);
    }

    private ArrayList<String> searchDomainsGrep(String pattern, int limit) throws IOException {
        var domains = new ArrayList<String>();
        var grep = new ProcessBuilder("grep", "-F", "-m", String.valueOf(limit), "-e", pattern, domainsListFile.toString())
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();
        try (var reader = new BufferedReader(new InputStreamReader(grep.getInputStream(), US_ASCII))) {
            reader.lines().forEach(domains::add);
            int exitValue = grep.waitFor();
            if (exitValue > 1) {
                throw new IOException("grep failed with exit code " + exitValue);
            }
        } catch (InterruptedException e) {
            throw new IOException("Interrupted waiting for grep", e);
        } finally {
            grep.destroyForcibly();
        }
        return domains;
    }

    private ArrayList<String> searchDomainsJava(String pattern, int limit) throws IOException {
        var domains = new ArrayList<String>();
        try (var reader = Files.newBufferedReader(domainsListFile, US_ASCII)) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.contains(pattern)) {
                    domains.add(line);
                    if (domains.size() >= limit) break;
                }
            }
        }
        return domains;
    }

    public static void main(String[] args) throws SQLException, IOException {
        Config config = new Config();
        config.setDataPath(Paths.get("data"));
        var searcher = new DomainSearcher(config);
//        du.loadDomainRanksCSV("/tmp/x");
        long start = System.currentTimeMillis();
        searcher.downloadAndLoadDomainRanks();
        System.out.println(searcher.searchDomains("canberra", 50));
        System.out.println("Took " + (System.currentTimeMillis() - start) + "ms");
    }

    public void ensureReady() throws IOException {
        if (failed) throw new IOException("Downloading domain list failed.");
        if (!Files.exists(domainsListFile)) {
            startDownloadAndLoadInBackground();
            throw new IOException("Domain list is now downloading. Please try again in a few minutes.");
        }
    }

    private synchronized void startDownloadAndLoadInBackground() {
        if (downloadThread == null) {
            downloadThread = new Thread(() -> {
                try {
                    downloadAndLoadDomainRanks();
                } catch (IOException e) {
                    log.error("Failed to download and load domain ranks", e);
                    failed = true;
                }
            }, "DomainSearcher.downloadAndLoadDomainRanks()");
            downloadThread.setDaemon(true);
            downloadThread.start();
        }
    }
}
