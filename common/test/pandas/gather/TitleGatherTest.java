package pandas.gather;

import org.junit.jupiter.api.Test;
import pandas.collection.Status;
import pandas.collection.Title;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TitleGatherTest {
    @Test
    public void testScheduling() {
        GatherSchedule monthly = new GatherSchedule("Monthly", 0, 1, 0);

        TitleGather gather = new TitleGather();
        gather.setTitle(new Title());
        gather.getTitle().setStatus(Status.NOMINATED);
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
}