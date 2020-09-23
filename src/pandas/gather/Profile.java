package pandas.gather;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PROFILE")
public class Profile {
    @Column(name = "NAME")
    private String name;

    @Column(name = "PROFILE_DESCRIPTION")
    private String profileDescription;

    @Id
    @Column(name = "PROFILE_ID")
    private Long profileId;

    @Column(name = "IS_DEFAULT")
    private Long isDefault;


    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileDescription() {
        return this.profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public Long getProfileId() {
        return this.profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Long getIsDefault() {
        return this.isDefault;
    }

    public void setIsDefault(Long isDefault) {
        this.isDefault = isDefault;
    }
}
