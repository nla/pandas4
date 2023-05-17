package pandas.social;

import org.netpreserve.jwarc.WarcWriter;

import java.io.IOException;

public interface WarcWriterSupplier {
    WarcWriter writer() throws IOException;
}
