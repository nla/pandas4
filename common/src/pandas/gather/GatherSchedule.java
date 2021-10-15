package pandas.gather;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.joining;

@Entity
@Table(name = "GATHER_SCHEDULE")
public class GatherSchedule implements Comparable<GatherSchedule> {
    public static final String ANNUAL = "Annual";
    public static final String DEFAULT = ANNUAL;
    private static final ZonedDateTime referenceTime = ZonedDateTime.parse("2020-01-01T00:00:00Z");

    @Id
    @Column(name = "GATHER_SCHEDULE_ID")
    @GenericField(aggregable = Aggregable.YES)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GATHER_SCHEDULE_SEQ")
    @SequenceGenerator(name = "GATHER_SCHEDULE_SEQ", sequenceName = "GATHER_SCHEDULE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "SCHEDULE_NAME")
    private String name;

    @Column(name = "YEARS")
    private int years;

    @Column(name = "MONTHS")
    private int months;

    @Column(name = "DAYS")
    private int days;

    @Column(name = "DAYS_OF_WEEK")
    private int daysOfWeek;

    @Column(name = "HOURS_OF_DAY")
    private int hoursOfDay;

    public GatherSchedule() {
    }

    public GatherSchedule(String name, int years, int months, int days) {
        this.name = name;
        this.years = years;
        this.months = months;
        this.days = days;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long gatherScheduleId) {
        this.id = gatherScheduleId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String scheduleName) {
        this.name = scheduleName;
    }

    public int getYears() {
        return years;
    }

    public void setYears(int years) {
        this.years = years;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(int daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public List<Integer> getDaysOfWeekList() {
        List<Integer> list = new ArrayList<>();
        for (int i = nextSetBit(daysOfWeek, 0); i < Integer.SIZE; i = nextSetBit(daysOfWeek, i + 1)) {
            list.add(i);
        }
        return list;
    }

    public void setDaysOfWeekList(List<Integer> days) {
        int value = 0;
        for (int day: days) {
            value |= 1 << day;
        }
        this.daysOfWeek = value;
    }

    public int getHoursOfDay() {
        return hoursOfDay;
    }

    public void setHoursOfDay(int hoursOfDay) {
        this.hoursOfDay = hoursOfDay;
    }

    public List<Integer> getHoursOfDayList() {
        List<Integer> list = new ArrayList<>();
        for (int i = nextSetBit(hoursOfDay, 0); i < Integer.SIZE; i = nextSetBit(hoursOfDay, i + 1)) {
            list.add(i);
        }
        return list;
    }

    public void setHoursOfDayList(List<Integer> hours) {
        int value = 0;
        for (int hour: hours) {
            value |= 1 << hour;
        }
        this.hoursOfDay = value;
    }

    @Override
    public int compareTo(GatherSchedule o) {
        return this.calculateNextTime(referenceTime).compareTo(o.calculateNextTime(referenceTime));
    }

    public Instant calculateNextTime(Instant prev) {
        if (isNone()) return null;
        if (prev == null) prev = Instant.now();
        return calculateNextTime(prev.atZone(ZoneId.systemDefault())).toInstant();
    }

    public boolean isNone() {
        return years == 0 && months == 0 && days == 0 && daysOfWeek == 0 && hoursOfDay == 0;
    }

    public ZonedDateTime calculateNextTime(ZonedDateTime prev) {
        ZonedDateTime next = prev.plusYears(years).plusMonths(months).plusDays(days);

        // find the next day of the week slot
        if (daysOfWeek != 0) {
            if (hoursOfDay == 0) next = next.plusDays(1);
            int day = nextSetBitWrap(daysOfWeek, next.getDayOfWeek().getValue() - 1);
            next = next.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(day + 1)));
        }

        // if it's a new day reset the time to midnight
        if (next.toLocalDate().isAfter(prev.toLocalDate())) {
            next = next.truncatedTo(ChronoUnit.DAYS);
        }

        // find the next hour slot we're eligible for
        if (hoursOfDay != 0) {
            int hour = nextSetBitWrap(hoursOfDay, next.getHour() + 1);
            if (hour <= next.getHour()) {
                next = next.plusDays(1);

                if (daysOfWeek != 0) {
                    int day = nextSetBitWrap(daysOfWeek, next.getDayOfWeek().getValue() - 1);
                    next = next.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(day + 1)));
                }
            }
            next = next.withHour(hour).truncatedTo(ChronoUnit.HOURS);
        }

        return next;
    }

    static int nextSetBitWrap(int bits, int start) {
        int i = nextSetBit(bits, start);
        return i >= Integer.SIZE ? nextSetBit(bits, 0) : i;
    }

    private static int nextSetBit(int bits, int start) {
        return Integer.numberOfTrailingZeros(bits & ((-1) << start));
    }

    public boolean hasDayOfWeek(int dayIndex) {
        return (daysOfWeek & (1 << dayIndex)) != 0;
    }

    public boolean hasHourOfDay(int hour) {
        return (hoursOfDay & (1 << hour)) != 0;
    }

    public String summary() {
        StringBuilder sb = new StringBuilder();
        var periodList = new ArrayList<String>();
        if (years != 0) periodList.add(years == 1 && months == 0 && days == 0 ? "year" : years + " years");
        if (months != 0) periodList.add(months == 1 && years == 0 && days == 0 ? "month" : months + " months");
        if (days != 0) periodList.add(days == 1 && years == 0 && months == 0 ? "day" : days + " days");
        if (!periodList.isEmpty()) {
            sb.append("every ");
            sb.append(String.join(", ", periodList));
        }
        if (daysOfWeek != 0) {
            sb.append(" on ");
            sb.append(getDaysOfWeekList().stream().map(day -> DayOfWeek.of(day + 1).getDisplayName(TextStyle.FULL, Locale.getDefault()))
                    .collect(joining(", ")));
        }
        if (hoursOfDay != 0) {
            sb.append(" at ");
            sb.append(getHoursOfDayList().stream().map(GatherSchedule::prettyHour).collect(joining(", ")));
        }
        return sb.toString().trim();
    }

    private static String prettyHour(int hour) {
        if (hour == 0) {
            return "midnight";
        } else if (hour == 12) {
            return "noon";
        } else if (hour < 12) {
            return hour + "am";
        } else {
            return (hour - 12) + "pm";
        }
    }
}
