package pandas.collection;

import pandas.core.Individual;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class ContactPerson extends Individual {
    @Column(name = "FUNCTION")
    private String function;

    @Column(name = "COMMENTS")
    private String comments;

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
}
