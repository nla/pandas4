package pandas.admin.marcexport;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.marc4j.MarcStreamWriter;
import org.marc4j.marc.Record;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class MarcOutputFormat implements OutputFormat {
    private final MarcStreamWriter out;

    MarcOutputFormat(HttpServerExchange http) throws IOException {
        http.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/marc");
        http.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "filename=pandas-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss")) + ".mrc");
        this.out = new MarcStreamWriter(http.getOutputStream(), "UTF-8");
    }

    public void write(Record record) throws IOException {
        out.write(record);
    }

    @Override
    public void notFound(long pi) throws IOException {
        // Can't do anything
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
