package pandas.collection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CONTACT_TYPE")
public class ContactType {
    public static final int INITIAL_REQUEST_ID = 2;
    public static final int FOLLOWUP_REQUEST_ID = 3;

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

    public boolean isPermissionRequest() {
        return id == INITIAL_REQUEST_ID || id == FOLLOWUP_REQUEST_ID;
    }
}
