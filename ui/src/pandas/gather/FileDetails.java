package pandas.gather;

import org.netpreserve.jwarc.WarcMetadata;
import org.netpreserve.jwarc.WarcReader;
import org.netpreserve.jwarc.WarcRequest;
import org.netpreserve.jwarc.WarcResponse;
import pandas.search.FileSeacher;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class FileDetails {
    public String requestWarcHeader;
    public String requestHttpHeader;
    public String requestBody;
    public String responseWarcHeader;
    public String responseHttpHeader;
    public String metadataWarcHeader;
    public String metadataBody;

    public FileDetails(FileSeacher.Result result, Path warc) throws IOException {
        try (var reader = new WarcReader(warc)) {
            if (result.responseOffset() != null) {
                reader.position(result.responseOffset());
                var response = (WarcResponse) reader.next().orElseThrow();
                responseWarcHeader = new String(response.serializeHeader(), ISO_8859_1).trim();
                responseHttpHeader = new String(response.http().serializeHeader(), ISO_8859_1).trim();
            }

            if (result.requestOffset() != null) {
                reader.position(result.requestOffset());
                var request = (WarcRequest) reader.next().orElseThrow();
                requestWarcHeader = new String(request.serializeHeader(), ISO_8859_1).trim();
                requestHttpHeader = new String(request.http().serializeHeader(), ISO_8859_1).trim();
                requestBody = new String(request.http().body().stream().readNBytes(10_000_000), ISO_8859_1).trim();
            }

            if (result.metadataOffset() != null) {
                reader.position(result.metadataOffset());
                var metadata = (WarcMetadata) reader.next().orElseThrow();
                metadataWarcHeader = new String(metadata.serializeHeader(), ISO_8859_1).trim();
                metadataBody = new String(metadata.body().stream().readNBytes(10_000_000), ISO_8859_1).trim();
            }
        }
    }
}
