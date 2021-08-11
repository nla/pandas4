package pandas.gather;

import org.hibernate.search.engine.backend.types.Aggregable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "PROFILE")
public class Profile {
    @Id
    @Column(name = "PROFILE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PROFILE_SEQ")
    @SequenceGenerator(name = "PROFILE_SEQ", sequenceName = "PROFILE_SEQ", allocationSize = 1)
    @GenericField(aggregable = Aggregable.YES)
    private Long id;

    @Column(name = "NAME")
    @NotBlank
    private String name;

    @Column(name = "PROFILE_DESCRIPTION")
    private String description;

    @Column(name = "IS_DEFAULT")
    private boolean isDefault;

    @Column
    private String heritrixConfig;

    @ManyToOne
    @JoinColumn(name = "GATHER_METHOD_ID")
    private GatherMethod gatherMethod;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String profileDescription) {
        this.description = profileDescription;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long profileId) {
        this.id = profileId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getHeritrixConfig() {
        return heritrixConfig;
    }

    public void setHeritrixConfig(String heritrixConfig) {
        if (heritrixConfig != null && heritrixConfig.isBlank()) {
            heritrixConfig = null;
        }
        this.heritrixConfig = heritrixConfig;
    }

    public GatherMethod getGatherMethod() {
        return gatherMethod;
    }

    public void setGatherMethod(GatherMethod gatherMethod) {
        this.gatherMethod = gatherMethod;
    }
}
