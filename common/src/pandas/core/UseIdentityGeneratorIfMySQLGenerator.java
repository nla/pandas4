package pandas.core;

import jakarta.persistence.SequenceGenerator;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.generator.OnExecutionGenerator;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.id.factory.spi.CustomIdGeneratorCreationContext;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.EnumSet;
import java.util.Properties;

public class UseIdentityGeneratorIfMySQLGenerator extends IdentityGenerator implements IdentifierGenerator, OnExecutionGenerator {
    private boolean mysql = false;
    private final String sequenceName;
    private String nextValQuery;

    public UseIdentityGeneratorIfMySQLGenerator() {
        sequenceName = null;
    }

    public UseIdentityGeneratorIfMySQLGenerator(UseIdentityGeneratorIfMySQL anno, Member annotatedMember, CustomIdGeneratorCreationContext context) {
        if (annotatedMember instanceof Field field) {
            var sequenceAnno = field.getAnnotation(SequenceGenerator.class);
            if (sequenceAnno == null) throw new IllegalArgumentException("Field must be annotated with @SequenceGenerator");
            this.sequenceName = sequenceAnno.sequenceName();
        } else {
            throw new IllegalArgumentException("Only fields are supported");
        }
    }

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        JdbcEnvironment jdbcEnvironment = serviceRegistry.getService(JdbcEnvironment.class);
        Dialect dialect = jdbcEnvironment.getDialect();
        mysql = dialect instanceof MySQLDialect;
        if (mysql) {
            nextValQuery = dialect.getSequenceSupport().getSequenceNextValString(sequenceName);
        }
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object obj) {
        return session.createNativeQuery(nextValQuery, Long.class).uniqueResult();
    }

    @Override
    public boolean generatedOnExecution() {
        return mysql;
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }

    @Override
    public boolean referenceColumnsInSql(Dialect dialect) {
        return mysql && super.referenceColumnsInSql(dialect);
    }

    @Override
    public boolean writePropertyValue() {
        return !mysql;
    }
}
