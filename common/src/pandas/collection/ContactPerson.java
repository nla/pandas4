package pandas.collection;

import pandas.core.Individual;
import pandas.core.Role;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
public class ContactPerson extends Individual {
    @Column(name = "FUNCTION")
    private String function;

    @Column(name = "COMMENTS")
    private String comments;

    @ManyToMany(mappedBy = "contactPeople")
    private Set<Title> titles = new HashSet<>();

    public String getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getFunction() {
        return this.function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Publisher getPublisher() {
        return getRole().getOrganisation().getPublisher();
    }

    public void setPublisher(Publisher publisher) {
        Role role = getRole();
        role.setType(Role.TYPE_CONTACT);
        role.setTitle("Publisher Contact");
        role.setOrganisation(publisher.getOrganisation());
    }

    @Override
    public String getName() {
        StringBuilder builder = new StringBuilder();
        builder.append(getNameGiven());
        return Stream.of(getNameTitle(), getNameGiven(), getNameFamily())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
    }

    public Set<Title> getTitles() {
        return Collections.unmodifiableSet(titles);
    }

    public void enforceBelongsToTitle(Title title) {
        if (!getTitles().contains(title)) {
            throw new IllegalStateException("Contact person doesn't belong to title");
        }
    }
}
