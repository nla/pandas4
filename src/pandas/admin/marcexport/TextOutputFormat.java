package pandas.admin.marcexport;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.marc4j.marc.Record;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

class TextOutputFormat implements OutputFormat {
    private final BufferedWriter out;

    TextOutputFormat(HttpServerExchange http) throws IOException {
        http.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        this.out = new BufferedWriter(new OutputStreamWriter(http.getOutputStream(), "UTF-8"));
    }

    public void write(Record record) throws IOException {
        out.write(record.toString());
        out.write("\n\n");
    }

    @Override
    public void notFound(long pi) throws IOException {
        out.write("Title nla.arc-" + pi + " not found\n");
    }

    public void close() throws IOException {
        out.close();
    }
}
