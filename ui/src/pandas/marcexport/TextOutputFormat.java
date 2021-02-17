package pandas.marcexport;

import org.marc4j.marc.Record;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

class TextOutputFormat implements OutputFormat {
    private final BufferedWriter out;

    TextOutputFormat(HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        this.out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
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
