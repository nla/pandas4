package pandas.gatherer.heritrix;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BrowsertrixGathererTest {

    @Test
    void encodeShellCommandForLogging() {
        assertEquals("one tw\\ o", BrowsertrixGatherer.encodeShellCommandForLogging(List.of("one", "tw o")));
    }
}