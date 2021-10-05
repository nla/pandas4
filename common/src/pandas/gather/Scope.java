package pandas.gather;

import javax.persistence.*;

@Entity
public class Scope {
    @Id
    @Column(name = "SCOPE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SCOPE_SEQ")
    @SequenceGenerator(name = "SCOPE_SEQ", sequenceName = "SCOPE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "NAME")
    private String name;
}
