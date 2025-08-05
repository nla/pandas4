package pandas.core;

import com.googlecode.flyway.core.Flyway;
import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
        String dbType = determineDatabaseType(jdbcUrl);
        if (dbType == null) return;
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("classpath:pandas/migrations/" + dbType);
        flyway.migrate();
    }

    public static @Nullable String determineDatabaseType(String jdbcUrl) {
        if (jdbcUrl.startsWith("jdbc:oracle:")) {
            return  "oracle";
        } else if (jdbcUrl.startsWith("jdbc:mysql:") || jdbcUrl.startsWith("jdbc:mariadb:")) {
            return  "mysql";
        } else {
            return null;
        }
    }
}
