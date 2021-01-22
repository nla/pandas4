package pandas.gather;

import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GatherScheduleTest {

    @Test
    public void calculateNextTime() {
        GatherSchedule schedule = new GatherSchedule();
        schedule.setDaysOfWeekList(List.of(2));
        schedule.setHoursOfDayList(List.of(2));

        ZonedDateTime t = ZonedDateTime.parse("2020-01-01T02:00:00Z");
        t = schedule.calculateNextTime(t);
        assertEquals(ZonedDateTime.parse("2020-01-08T02:00Z"), t);
    }
}