package pandas.collection;

import com.fasterxml.jackson.annotation.JsonView;
import pandas.core.Individual;
import pandas.core.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import pandas.core.View;

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
        Role existingRole = getRoleIfExists();
        if (existingRole == null || existingRole.getOrganisation() == null) return null;
        return existingRole.getOrganisation().getPublisher();
    }

    public void setPublisher(Publisher publisher) {
        Role role = getRole();
        role.setType(Role.TYPE_CONTACT);
        role.setTitle("Publisher Contact");
        role.setOrganisation(publisher.getOrganisation());
    }

    @Override
    public String getName() {
        return Stream.of(getNameTitle(), getNameGiven(), getNameFamily())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
    }

    @JsonView(View.Summary.class)
    public String getNameAndFunction() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        if (function != null) {
            builder.append(", ").append(function);
        }
        Role existingRole = getRoleIfExists();
        if (existingRole != null && existingRole.getOrganisation() != null) {
            builder.append(" (").append(existingRole.getOrganisation().getName()).append(")");
        }
        return builder.toString();
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
