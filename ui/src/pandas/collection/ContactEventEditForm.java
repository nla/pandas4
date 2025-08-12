package pandas.collection;

import pandas.agency.User;
import pandas.util.Strings;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ContactEventEditForm {
    private LocalDateTime date;
    private String note;
    private ContactPerson contactPerson;
    private User user;
    private ContactMethod method;
    private ContactType type;

    public ContactEventEditForm() {
    }

    public ContactEventEditForm(Instant date, String note, ContactPerson contactPerson,
                               User user, ContactMethod method, ContactType type) {
        this.date = date != null ? date.atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
        this.note = note;
        this.contactPerson = contactPerson;
        this.user = user;
        this.method = method;
        this.type = type;
    }

    public static ContactEventEditForm from(ContactEvent contactEvent) {
        return new ContactEventEditForm(
                contactEvent.getDate(),
                contactEvent.getNote(),
                contactEvent.getContactPerson(),
                contactEvent.getUser(),
                contactEvent.getMethod(),
                contactEvent.getType());
    }

    public ContactEvent build() {
        var contactEvent = new ContactEvent();
        applyTo(contactEvent);
        return contactEvent;
    }

    void applyTo(ContactEvent contactEvent) {
        contactEvent.setDate(date != null ? date.atZone(ZoneId.systemDefault()).toInstant() : null);
        contactEvent.setNote(Strings.clean(note));
        contactEvent.setContactPerson(contactPerson);
        contactEvent.setUser(user);
        contactEvent.setMethod(method);
        contactEvent.setType(type);
    }

    // Getters and setters
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getDateFormatted() {
        return date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : "";
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public ContactPerson getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(ContactPerson contactPerson) {
        this.contactPerson = contactPerson;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ContactMethod getMethod() {
        return method;
    }

    public void setMethod(ContactMethod method) {
        this.method = method;
    }

    public ContactType getType() {
        return type;
    }

    public void setType(ContactType type) {
        this.type = type;
    }
}