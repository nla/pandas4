package pandas.gather;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
public class Scope {
    public static final long ALL_PAGES_ON_THIS_WEBSITE_ID = 1L;
    public static final long DEFAULT_ID = ALL_PAGES_ON_THIS_WEBSITE_ID;

    @Id
    @Column(name = "SCOPE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SCOPE_SEQ")
    @SequenceGenerator(name = "SCOPE_SEQ", sequenceName = "SCOPE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "NAME", nullable = false)
    @NotBlank
    private String name;

    @Column(name = "DEPTH")
    private Integer depth;

    public Scope() {
    }

    public Scope(String name, Integer depth) {
        this.name = name;
        this.depth = depth;
    }

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

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }
}
