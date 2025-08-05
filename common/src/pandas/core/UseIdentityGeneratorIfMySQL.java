package pandas.core;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use the identity generator if the database is MySQL. Otherwise, use the sequence generator.
 */
@IdGeneratorType( UseIdentityGeneratorIfMySQLGenerator.class )
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UseIdentityGeneratorIfMySQL {
}
