package pandas.gather;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import pandas.core.UseIdentityGeneratorIfMySQL;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Status code and (optional) redirect URL for a seed. Typically loaded from the Heritrix seed report.
 */
@Entity
public class InstanceSeed {
    @Id
    @UseIdentityGeneratorIfMySQL
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INSTANCE_SEED_SEQ")
    @SequenceGenerator(name = "INSTANCE_SEED_SEQ", sequenceName = "INSTANCE_SEED_SEQ", allocationSize = 1)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "INSTANCE_ID", nullable = false)
    private Instance instance;
    @NotNull
    private String url;
    private Integer status;
    private String redirect;

    public InstanceSeed() {
    }

    private InstanceSeed(String url, int status, String redirect) {
        this.url = url;
        this.status = status;
        this.redirect = redirect;
    }

    public Long getId() {
        return id;
    }

    public Instance getInstance() {
        return instance;
    }

    public Integer getStatus() {
        return status;
    }

    public String getRedirect() {
        return redirect;
    }

    public boolean isError() {
        return status != null && (status >= 400 || status < 0);
    }

    public String getUrl() {
        return url;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public static List<InstanceSeed> parseHeritrixSeedReport(BufferedReader reader) throws IOException {
        String header = reader.readLine();
        if (!"[code] [status] [seed] [redirect]".equals(header)) {
            throw new IOException("Invalid seed report header: " + header);
        }
        var seeds = new ArrayList<InstanceSeed>();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            String[] fields = line.split(" ", 4);
            if (fields.length < 3) {
                throw new IOException("Seed report line has too few fields: " + line);
            }
            int status = Integer.parseInt(fields[0]);
            String url = fields[2];
            String redirect = null;
            if (fields.length > 3 && !fields[3].isEmpty()) {
                redirect = fields[3];
            }
            seeds.add(new InstanceSeed(url, status, redirect));
        }
        return seeds;
    }
}
