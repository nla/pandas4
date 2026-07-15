package pandas.collection;

import org.junit.jupiter.api.Test;
import pandas.gather.GatherMethod;
import pandas.gather.Instance;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TitleTest {
    @Test
    void firstGatherDateIsTheEarliestInstanceDate() {
        Title title = new Title();
        Instant first = Instant.parse("2024-01-02T03:04:05Z");
        Instant second = Instant.parse("2025-02-03T04:05:06Z");

        title.getInstances().add(new Instance(title, second, GatherMethod.HERITRIX));
        title.getInstances().add(new Instance(title, first, GatherMethod.HERITRIX));

        assertEquals(first, title.getFirstGatherDate());
    }

    @Test
    void firstGatherDateIsNullWithoutInstances() {
        assertNull(new Title().getFirstGatherDate());
    }
}
