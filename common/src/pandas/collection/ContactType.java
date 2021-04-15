package pandas.collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CONTACT_TYPE")
public class ContactType {
    @Id
    @Column(name = "CONTACT_TYPE_ID", nullable = false, precision = 0)
    private Long id;

    @Column(name = "CONTACT_TYPE", nullable = false, length = 32)
    private String name;

    @Column(name = "EMAIL_SUBJECT", nullable = false, length = 1024)
    private String emailSubject;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }
}
