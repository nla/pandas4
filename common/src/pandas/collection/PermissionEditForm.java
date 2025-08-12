package pandas.collection;

import org.springframework.format.annotation.DateTimeFormat;
import pandas.util.Strings;

import java.time.LocalDate;

// TODO: Unfortunately on Spring Boot < 3.1 this has to be a mutable class because Spring can't bind nested
//       records. Once we Spring Boot 3.1 is released and we upgrade to it we can make this a record again.
public class PermissionEditForm {
    private String stateName = PermissionState.UNKNOWN;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate statusSetDate;
    private String localReference;
    private String domain;
    private String note;
    private ContactPerson contactPerson;
    private ContactPersonEditForm newTitleContact;
    private ContactPersonEditForm newPublisherContact;

    public static PermissionEditForm from(Permission permission) {
        var form = new PermissionEditForm();
        if (permission.getStateName() != null) form.stateName = permission.getStateName();
        form.statusSetDate = permission.getStatusSetLocalDate();
        form.localReference = permission.getLocalReference();
        form.domain = permission.getDomain();
        form.note = permission.getNote();
        form.contactPerson = permission.getContactPerson();
        return form;
    }

    void applyTo(Permission permission) {
        permission.setStateName(Strings.clean(stateName));
        permission.setStatusSetLocalDate(statusSetDate);
        permission.setLocalReference(Strings.clean(localReference));
        permission.setNote(Strings.clean(note));
        permission.setContactPerson(contactPerson);
        if (permission.isBlanket()) permission.setDomain(Strings.clean(domain));
    }

    public String getStateName() {
        return stateName;
    }

    public PermissionEditForm setStateName(String stateName) {
        this.stateName = stateName;
        return this;
    }

    public LocalDate getStatusSetDate() {
        return statusSetDate;
    }

    public PermissionEditForm setStatusSetDate(LocalDate statusSetDate) {
        this.statusSetDate = statusSetDate;
        return this;
    }

    public String getLocalReference() {
        return localReference;
    }

    public PermissionEditForm setLocalReference(String localReference) {
        this.localReference = localReference;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public PermissionEditForm setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getNote() {
        return note;
    }

    public PermissionEditForm setNote(String note) {
        this.note = note;
        return this;
    }

    public ContactPerson getContactPerson() {
        return contactPerson;
    }

    public PermissionEditForm setContactPerson(ContactPerson contactPerson) {
        this.contactPerson = contactPerson;
        return this;
    }

    public ContactPersonEditForm getNewTitleContact() {
        return newTitleContact;
    }

    public void setNewTitleContact(ContactPersonEditForm newTitleContact) {
        this.newTitleContact = newTitleContact;
    }

    public ContactPersonEditForm getNewPublisherContact() {
        return newPublisherContact;
    }

    public void setNewPublisherContact(ContactPersonEditForm newPublisherContact) {
        this.newPublisherContact = newPublisherContact;
    }
}
