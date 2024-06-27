package pandas.core;

import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.session.jdbc.OracleJdbcIndexedSessionRepositoryCustomizer;

@Configuration
public class SessionConfig {
    // This is a workaround for an exception we get seemingly randomly hit occasionally when sessions
    // are first created and the session insert fails with duplicate key exception.
    // Seems to be a relatively common issue with Spring Session: https://stackoverflow.com/a/69540376
    @Bean
    @Conditional(OracleDatabaseCondition.class)
    public OracleJdbcIndexedSessionRepositoryCustomizer oracleJdbcIndexedSessionRepositoryCustomizer() {
        return new OracleJdbcIndexedSessionRepositoryCustomizer();
    }

    public static class OracleDatabaseCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String jdbcUrl = context.getEnvironment().getProperty("spring.datasource.url");
            return jdbcUrl != null && jdbcUrl.startsWith("jdbc:oracle:");
        }
    }
}
