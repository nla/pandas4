package pandas.collection;

import pandas.gather.GatherSchedule;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

public record CollectionEditForm(@NotBlank String name, List<Subject> subjects,
                                 String description, Integer startMonth,
                                 Integer startYear, Integer endMonth, Integer endYear,
                                 Collection parent, GatherSchedule gatherSchedule,
                                 boolean closed) {
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
                startMonth, startYear, endMonth, endYear, collection.getParent(), collection.getGatherSchedule(),
                collection.isClosed());
    }

    public void applyTo(Collection collection) {
        collection.setName(name.trim());
        collection.setSubjects(subjects);
        collection.setDescription(description != null && !description.isBlank() ? description.trim() : null);
        collection.setParent(parent);
        if (gatherSchedule != null) {
            collection.setGatherSchedule(gatherSchedule);
        }
        if (startYear != null) {
            collection.setStartDate(LocalDate.of(startYear, startMonth == null ? 1 : startMonth, 1)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            collection.setStartDate(null);
        }
        if (endYear != null) {
            collection.setEndDate(LocalDate.of(endYear, endMonth == null ? 12 : endMonth, 1)
                    .with(lastDayOfMonth())
                    .atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        } else {
            collection.setEndDate(null);
        }
        collection.setClosed(closed);
    }
}
