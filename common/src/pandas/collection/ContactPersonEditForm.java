package pandas.collection;

import pandas.util.Strings;

public class ContactPersonEditForm {
    private String nameTitle;
    private String nameGiven;
    private String nameFamily;
    private String function;
    private String phone;
    private String mobilePhone;
    private String fax;
    private String email;

    public ContactPersonEditForm() {
    }

    public ContactPersonEditForm(String nameTitle, String nameGiven, String nameFamily, String function, 
                                String phone, String mobilePhone, String fax, String email) {
        this.nameTitle = nameTitle;
        this.nameGiven = nameGiven;
        this.nameFamily = nameFamily;
        this.function = function;
        this.phone = phone;
        this.mobilePhone = mobilePhone;
        this.fax = fax;
        this.email = email;
    }

    public static ContactPersonEditForm from(ContactPerson contactPerson) {
        return new ContactPersonEditForm(
                contactPerson.getNameTitle(),
                contactPerson.getNameGiven(),
                contactPerson.getNameFamily(),
                contactPerson.getFunction(),
                contactPerson.getPhone(),
                contactPerson.getMobilePhone(),
                contactPerson.getFax(),
                contactPerson.getEmail());
    }

    public ContactPerson build() {
        var contactPerson = new ContactPerson();
        applyTo(contactPerson);
        return contactPerson;
    }

    void applyTo(ContactPerson contactPerson) {
        contactPerson.setNameTitle(Strings.clean(nameTitle));
        contactPerson.setNameGiven(Strings.clean(nameGiven));
        contactPerson.setNameFamily(Strings.clean(nameFamily));
        contactPerson.setFunction(Strings.clean(function));
        contactPerson.setMobilePhone(Strings.clean(mobilePhone));
        contactPerson.setPhone(Strings.clean(phone));
        contactPerson.setFax(Strings.clean(fax));
        contactPerson.setEmail(Strings.clean(email));
    }

    public String getNameTitle() {
        return nameTitle;
    }

    public void setNameTitle(String nameTitle) {
        this.nameTitle = nameTitle;
    }

    public String getNameGiven() {
        return nameGiven;
    }

    public void setNameGiven(String nameGiven) {
        this.nameGiven = nameGiven;
    }

    public String getNameFamily() {
        return nameFamily;
    }

    public void setNameFamily(String nameFamily) {
        this.nameFamily = nameFamily;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isNameBlank() {
        return nameGiven.isBlank() && nameFamily.isBlank();
    }
}
