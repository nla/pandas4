package pandas.collection;

import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

public class CollectionEditForm {
    @NotBlank
    private final String name;
    private final List<Subject> subjects;
    private final String description;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public CollectionEditForm(String name, List<Subject> subjects, String description,
                              @DateTimeFormat(iso = DATE) LocalDate startDate,
                              @DateTimeFormat(iso = DATE) LocalDate endDate) {
        this.name = name;
        this.subjects = subjects;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static CollectionEditForm of(Collection collection) {
        return new CollectionEditForm(collection.getName(), collection.getSubjects(), collection.getDescription(),
                collection.getStartDate() == null ? null : collection.getStartDate().atZone(ZoneId.systemDefault()).toLocalDate(),
                collection.getEndDate() == null ? null : collection.getEndDate().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public void applyTo(Collection collection) {
        collection.setName(name.trim());
        collection.setSubjects(subjects);
        collection.setDescription(description != null && !description.isBlank() ? description.trim() : null);
        collection.setStartDate(startDate == null ? null : startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        collection.setEndDate(endDate == null ? null : endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
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

    public LocalDate startDate() {
        return startDate;
    }

    public LocalDate endDate() {
        return endDate;
    }
}
