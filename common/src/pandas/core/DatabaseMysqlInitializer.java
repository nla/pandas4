package pandas.core;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Conditionally sets JPA mapping resources for MySQL and MariaDB.
 */
public class DatabaseMysqlInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String datasourceUrl = environment.getProperty("spring.datasource.url");

        if (datasourceUrl != null && (datasourceUrl.startsWith("jdbc:mysql:") || datasourceUrl.startsWith("jdbc:mariadb:"))) {
            System.setProperty("spring.jpa.mapping-resources", "pandas/orm-mysql.xml");
        }
    }
}