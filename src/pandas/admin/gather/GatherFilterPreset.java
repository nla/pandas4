package pandas.admin.gather;

import javax.persistence.*;

@Entity
@Table(name = "GATHER_FILTER_PRESET")
public class GatherFilterPreset {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GATHER_FILTER_PRESET_SEQ")
    @SequenceGenerator(name = "GATHER_FILTER_PRESET_SEQ", sequenceName = "GATHER_FILTER_PRESET_SEQ", allocationSize = 1)
    @Column(name = "GATHER_FILTER_PRESET_ID")
    private Long id;

    @Column(name = "FILTER_NAME")
    private String name;

    @Column(name = "FILTER_PRESET")
    private String filters;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }
}
