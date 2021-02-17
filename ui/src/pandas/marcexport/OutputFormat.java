package pandas.marcexport;

import org.marc4j.marc.Record;

import java.io.Closeable;
import java.io.IOException;

interface OutputFormat extends Closeable {
    void write(Record record) throws IOException;

    void notFound(long pi) throws IOException;
}
