package pandas.social;

import org.netpreserve.jwarc.WarcCompression;
import org.netpreserve.jwarc.WarcWriter;
import org.netpreserve.jwarc.Warcinfo;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardOpenOption.*;

/**
 * Manages the lifecycle of a WARC file we're writing, including uploading it to Bamboo and indexing it.
 * <p>
 * While this class keeps track of the size and time limits the actual decision to close and upload the file is made
 * by the caller. This is so the caller can decide to finish the file at an appropriate boundary and persist its
 * state in the database.
 */
public class BambooWarcManager implements Closeable, WarcWriterSupplier {
    private final BambooClient bambooClient;
    private final long crawlId;
    private final String basename;
    private final SocialIndexer socialIndexer;
    private final SocialConfig socialConfig;
    private FileChannel channel;
    private WarcWriter warcWriter;
    int fileSeqNo = 0;
    private String filename;
    private long startPosition;
    private long startTimeMillis;

    public BambooWarcManager(BambooClient bambooClient, long crawlId, String basename, SocialIndexer socialIndexer,
                             SocialConfig socialConfig) {
        this.bambooClient = bambooClient;
        this.crawlId = crawlId;
        this.basename = basename;
        this.socialIndexer = socialIndexer;
        this.socialConfig = socialConfig;
    }

    private void beginNewFile() throws IOException {
        if (channel != null) {
            throw new IllegalStateException("file already open");
        }
        this.filename = basename + "-" + fileSeqNo + ".warc.gz";
        fileSeqNo += 1;
        Path tempFile = Files.createTempFile("pandas-social-", ".warc.gz");
        channel = FileChannel.open(tempFile, DELETE_ON_CLOSE, READ, WRITE);
        warcWriter = new WarcWriter(channel, WarcCompression.GZIP);
        warcWriter.write(new Warcinfo.Builder()
                .filename(filename)
                .fields(Map.of("software", List.of("pandas-social")))
                .build());
        this.startPosition = warcWriter.position();
        this.startTimeMillis = System.currentTimeMillis();
    }

    public void uploadCurrentFile() throws IOException {
        if (warcWriter == null || warcWriter.position() <= startPosition) {
            return; // we didn't actually write anything, so nothing to do
        }
        channel.position(0);
        long warcId = bambooClient.putWarcIfNotExists(crawlId, filename, channel, channel.size());
        socialIndexer.enqueueWarcId(warcId);
        closeCurrentFile();
    }

    public WarcWriter writer() throws IOException {
        if (warcWriter == null) {
            beginNewFile();
        }
        return warcWriter;
    }

    public String currentFilename() {
        return filename;
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            closeCurrentFile();
        }
    }

    private void closeCurrentFile() throws IOException {
        channel.close();
        channel = null;
        warcWriter = null;
        filename = null;
    }

    public boolean hasReachedLimit() {
        return warcWriter != null && (warcWriter.position() > socialConfig.getWarcSizeLimitBytes() ||
                System.currentTimeMillis() - startTimeMillis > socialConfig.getWarcTimeLimitMillis());
    }
}
