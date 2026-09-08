package pandas.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@ConditionalOnExpression("'${spring.datasource.url}'.startsWith('jdbc:oracle:')")
@Configuration
public class DatabaseOracleConfig {
    @Bean
    HibernatePropertiesCustomizer mysqlHibernateTz() {
        return props -> {
            // use TIMESTAMP instead of TIMESTAMP_UTC as the legacy Oracle database has local times
            // but going forward when we migrate to MySQL we want to use UTC
            props.put("hibernate.type.preferred_instant_jdbc_type", "TIMESTAMP");
        };
    }
}
