package pandas.core;

import com.googlecode.flyway.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
@Profile("!test")
public class FlywayConfig {
    private final DataSource dataSource;
    private final String jdbcUrl;

    public FlywayConfig(DataSource dataSource, @Value("${spring.datasource.url}") String jdbcUrl) {
        this.dataSource = dataSource;
        this.jdbcUrl = jdbcUrl;
    }

    @PostConstruct
    public void init() {
        if (!jdbcUrl.startsWith("jdbc:oracle:")) return;
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("classpath:pandas/migrations/oracle");
        flyway.migrate();
    }
}
