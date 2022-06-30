package pandas.core;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Rewrites native queries where Oracle syntax differs from standard SQL.
 *
 * For example the keyword RECURSIVE in 'WITH RECURSIVE cte(...)' is disallowed by Oracle but required by Postgres.
 */
@Component
@ConditionalOnExpression("'${spring.datasource.url}'.startsWith('jdbc:oracle:')")
public class OracleCompatQueryRewriter implements HibernatePropertiesCustomizer {
    private String rewriteQuery(String sql) {
        // these are not intended to match every possible query, just the ones we use in practice
        return sql.replace("with recursive", "with")
                .replace(" = true", " = 1");
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.session_factory.statement_inspector", (StatementInspector)
                this::rewriteQuery);
    }
}
