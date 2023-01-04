package pandas.core;

import org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.exec.spi.JdbcSelect;
import org.hibernate.sql.results.spi.ListResultsConsumer;
import org.hibernate.sql.results.spi.RowTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.thymeleaf.spring6.util.FieldUtils;

import java.util.List;
import java.util.Properties;

public class DatabasePropertiesListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private final Logger log = LoggerFactory.getLogger(DatabasePropertiesListener.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        if (environment.getProperty("spring.datasource.url", "").startsWith("jdbc:oracle:")) {
            return; // Use Flyway
        }
        log.info("Not using Flyway so allowing Hibernate to manage the database schema");
        Properties props = new Properties();
        props.put("spring.jpa.hibernate.ddl-auto", "update");
        props.put("spring.session.jdbc.initialize-schema", "always");
        props.put("spring.datasource.initialization-mode", "always");
        environment.getPropertySources().addFirst(new PropertiesPropertySource("dynamicDbProps", props));
    }
}
