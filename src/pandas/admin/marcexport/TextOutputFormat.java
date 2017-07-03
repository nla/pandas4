package pandas.admin.marcexport;

import org.marc4j.marc.Record;
import spark.Response;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

class TextOutputFormat implements OutputFormat {
    private final BufferedWriter out;

    TextOutputFormat(Response res) throws IOException {
        res.type("text/plain");
        this.out = new BufferedWriter(new OutputStreamWriter(res.raw().getOutputStream(), "UTF-8"));
    }

    public void write(Record record) throws IOException {
        out.write(record.toString());
        out.write("\n\n");
    }

    @Override
    public void notFound(long pi) throws IOException {
        out.write("Title nla.arc-" + pi + " not found");
    }

    public void close() throws IOException {
        out.flush();
    }
}
