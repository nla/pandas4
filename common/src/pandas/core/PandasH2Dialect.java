package pandas.core;

import org.hibernate.dialect.H2Dialect;

import java.sql.Types;

public class PandasH2Dialect extends H2Dialect {
    public PandasH2Dialect() {
        super();

        // workaround for H2Dialect specifying a length larger than H2's maximum (10248576)
        if (getTypeName(Types.LONGVARCHAR).equals("varchar(2147483647)")) {
            registerColumnType(Types.LONGVARCHAR, "varchar($l)");
        }
    }
}
