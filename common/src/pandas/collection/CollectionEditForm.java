package pandas.collection;

import pandas.gather.GatherSchedule;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

public class CollectionEditForm {
    @NotBlank
    private final String name;
    private final List<Subject> subjects;
    private final String description;
    private final Integer startMonth;
    private final Integer startYear;
    private final Integer endMonth;
    private final Integer endYear;
    private final Collection parent;
    private final GatherSchedule gatherSchedule;

    public CollectionEditForm(String name, List<Subject> subjects, String description, Integer startMonth,
                              Integer startYear, Integer endMonth, Integer endYear, Collection parent, GatherSchedule gatherSchedule) {
        this.name = name;
        this.subjects = subjects;
        this.description = description;
        this.startMonth = startMonth;
        this.startYear = startYear;
        this.endMonth = endMonth;
        this.endYear = endYear;
        this.parent = parent;
        this.gatherSchedule = gatherSchedule;
    }

    public static CollectionEditForm of(Collection collection) {
        Integer startMonth = null, startYear = null;
        if (collection.getStartDate() != null) {
            LocalDate startDate = collection.getStartDate().atZone(ZoneId.systemDefault()).toLocalDate();
            startMonth = startDate.getMonthValue();
            startYear = startDate.getYear();
        }
        Integer endMonth = null, endYear = null;
        if (collection.getEndDate() != null) {
            LocalDate endDate = collection.getEndDate().atZone(ZoneId.systemDefault()).toLocalDate();
            endMonth = endDate.getMonthValue();
            endYear = endDate.getYear();
        }
        return new CollectionEditForm(collection.getName(), collection.getSubjects(), collection.getDescription(),
                startMonth, startYear, endMonth, endYear, collection.getParent(), collection.getGatherSchedule());
    }

    public void applyTo(Collection collection) {
        collection.setName(name.trim());
        collection.setSubjects(subjects);
        collection.setDescription(description != null && !description.isBlank() ? description.trim() : null);
        collection.setParent(parent);
        collection.setGatherSchedule(gatherSchedule);
        if (startYear != null) {
            collection.setStartDate(LocalDate.of(startYear, startMonth == null ? 1 : startMonth, 1)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        if (endYear != null) {
            collection.setEndDate(LocalDate.of(endYear, endMonth == null ? 12 : endMonth, 1)
                    .with(lastDayOfMonth())
                    .atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        }
    }

    public String name() {
        return name;
    }

    public List<Subject> subjects() {
        return subjects;
    }

    public String description() {
        return description;
    }

    public Integer startMonth() {
        return startMonth;
    }

    public Integer startYear() {
        return startYear;
    }

    public Integer endMonth() {
        return endMonth;
    }

    public Integer endYear() {
        return endYear;
    }

    public Collection parent() {
        return parent;
    }

    public GatherSchedule gatherSchedule() {
        return gatherSchedule;
    }
}
