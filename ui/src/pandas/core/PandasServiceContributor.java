package pandas.core;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.internal.JdbcServicesImpl;
import org.hibernate.engine.jdbc.internal.JdbcServicesInitiator;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.service.spi.ServiceContributor;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcOperationQuerySelect;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.exec.spi.JdbcSelectExecutor;
import org.hibernate.sql.results.spi.ListResultsConsumer;
import org.hibernate.sql.results.spi.RowTransformer;

import java.util.List;
import java.util.Map;

public class PandasServiceContributor implements ServiceContributor {
    @Override
    public void contribute(StandardServiceRegistryBuilder serviceRegistryBuilder) {
        serviceRegistryBuilder.addInitiator(new JdbcServicesInitiator() {
            @Override
            public JdbcServices initiateService(Map<String, Object> configurationValues, ServiceRegistryImplementor registry) {
                JdbcSelectExecutorStandardImpl instance = new JdbcSelectExecutorStandardImpl() {
                    @Override
                    public <R> List<R> list(JdbcOperationQuerySelect jdbcSelect, JdbcParameterBindings jdbcParameterBindings, ExecutionContext executionContext, RowTransformer<R> rowTransformer, Class<R> domainResultType, ListResultsConsumer.UniqueSemantic uniqueSemantic) {
                        long startNanos = System.nanoTime();
                        List<R> list = super.list(jdbcSelect, jdbcParameterBindings, executionContext, rowTransformer, domainResultType, uniqueSemantic);
                        long elapsedNanos = System.nanoTime() - startNanos;

                        RequestLogger.Context context = RequestLogger.context.get();
                        context.afterSqlExecute(jdbcSelect::getSqlString, () -> executionContext.getQueryIdentifier(""),
                                elapsedNanos, list.size());
                        return list;
                    }
                };
                return new JdbcServicesImpl() {
                    @Override
                    public JdbcSelectExecutor getJdbcSelectExecutor() {
                        return instance;
                    }
                };
            }
        });

    }
}
