package pandas.gather;

import org.junit.jupiter.api.Test;
import pandas.collection.Status;
import pandas.collection.Title;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TitleGatherTest {
    @Test
    public void testScheduling() {
        GatherSchedule monthly = new GatherSchedule("Monthly", 0, 1, 0);

        TitleGather gather = new TitleGather();
        gather.setTitle(new Title());
        gather.getTitle().changeStatus(Status.NOMINATED, null, null, Instant.now());
        gather.setScheduledDate(LocalDateTime.of(2012, 1, 1, 9, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        gather.setSchedule(monthly);
        gather.calculateNextGatherDate();
        assertEquals(LocalDate.of(2012, 1, 1), gather.getNextGatherDate().atZone(ZoneId.systemDefault()).toLocalDate());

        gather.setLastGatherDate(LocalDateTime.of(2012, 1, 2, 9, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        gather.calculateNextGatherDate();
        assertEquals(LocalDate.of(2012, 2, 2), gather.getNextGatherDate().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    @Test
    public void testBuildHttrackCommand() {
        Title title = new Title();
        title.setTitleUrl("http://example.com/?");
        TitleGather gather = new TitleGather();
        gather.setTitle(title);
        gather.setIgnoreRobotsTxt(true);
        assertEquals("--robots=0 'http://example.com/?'", gather.buildHttrackCommandString());
    }

    @Test
    public void testChangeStatusToCeasedClearsNextGatherDate() {
        GatherSchedule monthly = new GatherSchedule("Monthly", 0, 1, 0);

        Title title = new Title();
        TitleGather gather = new TitleGather();
        title.setGather(gather);
        gather.setTitle(title);
        title.changeStatus(Status.NOMINATED, null, null, Instant.now());
        gather.setScheduledDate(LocalDateTime.of(2012, 1, 1, 9, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        gather.setSchedule(monthly);
        gather.calculateNextGatherDate();
        assertEquals(LocalDate.of(2012, 1, 1), gather.getNextGatherDate().atZone(ZoneId.systemDefault()).toLocalDate());

        title.changeStatus(Status.CEASED, null, null, Instant.now());
        assertNull(gather.getNextGatherDate());
    }
}
