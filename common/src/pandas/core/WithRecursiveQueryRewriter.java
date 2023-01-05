package pandas.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.QueryRewriter;
import org.springframework.stereotype.Component;

@Component
public
class WithRecursiveQueryRewriter implements QueryRewriter {

    private final boolean databaseIsOracle;

    public WithRecursiveQueryRewriter(@Value("${spring.datasource.url}") String jdbcUrl) {
        databaseIsOracle = jdbcUrl.startsWith("jdbc:oracle:");
    }

    @Override
    public String rewrite(String query, Sort sort) {
        // Oracle doesn't understand "with recursive" but most other databases require it.
        // So we write our queries with it and then remove it if we're on Oracle.
        if (databaseIsOracle) {
            return query.replace("with recursive ", "with ");
        } else {
            return query;
        }
    }
}
