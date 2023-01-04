package pandas.collection;

import pandas.core.Organisation;

import jakarta.validation.constraints.NotBlank;

import static pandas.util.Strings.emptyToNull;

public record PublisherEditForm(Long id, @NotBlank String name, PublisherType type,
                                String url, String abn, String localReference,
                                String notes, String addressLine1, String addressLine2,
                                String locality, String state, String postcode,
                                String country) {
    public PublisherEditForm(Long id, String name, PublisherType type, String url, String abn, String localReference, String notes, String addressLine1, String addressLine2, String locality, String state, String postcode, String country) {
        this.id = id;
        this.name = emptyToNull(name);
        this.type = type;
        this.url = emptyToNull(url);
        this.abn = emptyToNull(abn);
        this.localReference = emptyToNull(localReference);
        this.notes = emptyToNull(notes);
        this.addressLine1 = emptyToNull(addressLine1);
        this.addressLine2 = emptyToNull(addressLine2);
        this.locality = emptyToNull(locality);
        this.state = emptyToNull(state);
        this.postcode = emptyToNull(postcode);
        this.country = emptyToNull(country);
    }

    public static PublisherEditForm of(Publisher publisher) {
        Organisation organisation = publisher.getOrganisation();
        return new PublisherEditForm(publisher.getId(), publisher.getName(), publisher.getType(),
                organisation.getUrl(), organisation.getAbn(), publisher.getLocalReference(), publisher.getNotes(),
                organisation.getLine1(), organisation.getLine2(),
                organisation.getLocality(), organisation.getLongstate(),
                organisation.getPostcode(), organisation.getLongcountry());
    }

    public void applyTo(Publisher publisher) {
        publisher.getOrganisation().setName(name);
        publisher.setType(type);
        publisher.getOrganisation().setUrl(url);
        publisher.getOrganisation().setAbn(abn);
        publisher.setLocalReference(localReference);
        publisher.setNotes(notes);
        publisher.getOrganisation().setLine1(addressLine1);
        publisher.getOrganisation().setLine2(addressLine2);
        publisher.getOrganisation().setLocality(locality);
        publisher.getOrganisation().setLongstate(state);
        publisher.getOrganisation().setPostcode(postcode);
        publisher.getOrganisation().setLongcountry(country);
    }
}
