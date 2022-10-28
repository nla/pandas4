package pandas.collection;

import pandas.util.Strings;

public record ContactPersonEditForm(
        String nameTitle,
        String nameGiven,
        String nameFamily,
        String function,
        String phone,
        String mobilePhone,
        String fax,
        String email
) {

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
}
