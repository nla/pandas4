package pandas.core;

import com.googlecode.flyway.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {
    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource, @Value("${spring.datasource.url}") String jdbcUrl) {
        if (!jdbcUrl.startsWith("jdbc:oracle:")) return null;
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("classpath:pandas/migrations/oracle");
        return flyway;
    }
}
