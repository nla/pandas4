package pandas.collection;

public class PublisherEditForm {
    private final String name;
    private final PublisherType type;
    private final String url;
    private final String localReference;
    private final String notes;
    private final String addressLine1;
    private final String addressLine2;
    private final String locality;
    private final String state;
    private final String postcode;
    private final String country;

    public PublisherEditForm(String name, PublisherType type, String url, String localReference, String notes, String addressLine1, String addressLine2, String locality, String state, String postcode, String country) {
        this.name = name;
        this.type = type;
        this.url = url;
        this.localReference = localReference;
        this.notes = notes;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.locality = locality;
        this.state = state;
        this.postcode = postcode;
        this.country = country;
    }

    public PublisherEditForm(Publisher publisher) {
        name = publisher.getName();
        type = publisher.getType();
        url = publisher.getOrganisation().getUrl();
        localReference = publisher.getLocalReference();
        notes = publisher.getNotes();
        addressLine1 = publisher.getOrganisation().getLine1();
        addressLine2 = publisher.getOrganisation().getLine2();
        locality = publisher.getOrganisation().getLocality();
        state = publisher.getOrganisation().getLongstate();
        postcode = publisher.getOrganisation().getPostcode();
        country = publisher.getOrganisation().getLongcountry();
    }

    public void applyTo(Publisher publisher) {
        publisher.getOrganisation().setName(name);
        publisher.setType(type);
        publisher.getOrganisation().setUrl(url);
        publisher.setLocalReference(localReference);
        publisher.setNotes(notes);
        publisher.getOrganisation().setLine1(addressLine1);
        publisher.getOrganisation().setLine2(addressLine2);
        publisher.getOrganisation().setLocality(locality);
        publisher.getOrganisation().setLongstate(state);
        publisher.getOrganisation().setPostcode(postcode);
        publisher.getOrganisation().setLongcountry(country);
    }

    public String name() {
        return name;
    }

    public PublisherType type() {
        return type;
    }

    public String url() {
        return url;
    }

    public String localReference() {
        return localReference;
    }

    public String notes() {
        return notes;
    }

    public String addressLine1() {
        return addressLine1;
    }

    public String addressLine2() {
        return addressLine2;
    }

    public String locality() {
        return locality;
    }

    public String state() {
        return state;
    }

    public String postcode() {
        return postcode;
    }

    public String country() {
        return country;
    }
}
