package pandas.marcexport;

import org.marc4j.MarcStreamWriter;
import org.marc4j.marc.Record;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class MarcOutputFormat implements OutputFormat {
    private final MarcStreamWriter out;

    MarcOutputFormat(HttpServletResponse response) throws IOException {
        response.setContentType("application/marc");
        response.setHeader("Content-Disposition", "filename=pandas-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss")) + ".mrc");
        this.out = new MarcStreamWriter(response.getOutputStream(), "UTF-8");
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
