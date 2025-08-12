package pandas.collection;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import pandas.core.Resolver;
import pandas.util.Strings;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Validated
public record ContactEventEditForm(
    LocalDateTime date,
    String note,
    @NotNull Long contactPersonId,
    @NotNull Long methodId,
    @NotNull Long typeId
) {
    public static ContactEventEditForm from(ContactEvent contactEvent) {
        return new ContactEventEditForm(
                contactEvent.getDate() != null ? contactEvent.getDate().atZone(ZoneId.systemDefault()).toLocalDateTime() : null,
                contactEvent.getNote(),
                contactEvent.getContactPerson() == null ? null : contactEvent.getContactPerson().getId(),
                contactEvent.getMethod() == null ? null : contactEvent.getMethod().getId(),
                contactEvent.getType() == null ? null : contactEvent.getType().getId());
    }

    void applyTo(ContactEvent contactEvent, Resolver resolver) {
        contactEvent.setDate(date != null ? date.atZone(ZoneId.systemDefault()).toInstant() : null);
        contactEvent.setNote(Strings.clean(note));
        contactEvent.setContactPerson(resolver.ref(ContactPerson.class, contactPersonId));
        contactEvent.setMethod(resolver.ref(ContactMethod.class, methodId));
        contactEvent.setType(resolver.ref(ContactType.class, typeId));
    }

    public String getDateFormatted() {
        return date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : "";
    }

}