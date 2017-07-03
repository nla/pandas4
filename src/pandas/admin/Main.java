package pandas.admin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.skife.jdbi.v2.DBI;
import pandas.admin.gather.FilterPresetController;
import pandas.admin.marcexport.MarcExport;
import pandas.admin.marcexport.PandasDAO;
import spark.Spark;

public class Main {

    public static void main(String args[]) {
        // Workaround for Oracle JDBC library returning their own TIMESTAMP class instead of a Java Date
        System.setProperty("oracle.jdbc.J2EE13Compliant", "true");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("PandasDb");
        hikariConfig.setJdbcUrl(env("PANDAS_DB_URL"));
        hikariConfig.setUsername(env("PANDAS_DB_USER"));
        hikariConfig.setPassword(env("PANDAS_DB_PASSWORD"));
        hikariConfig.setMaximumPoolSize(1);
        DBI dbi = new DBI(new HikariDataSource(hikariConfig));
        PandasDAO dao = dbi.onDemand(PandasDAO.class);

        Spark.ipAddress(env("BIND_ADDRESS", "127.0.0.1"));
        Spark.port(Integer.parseInt(env("PORT", "3001")));

        //On.staticFilesLookIn("static", "META-INF/resources");
        Spark.staticFileLocation("META-INF/resources");

        Spark.get("/", (req, res) -> {
           res.redirect("/admin/marcexport");
           return null;
        });

        new MarcExport(dao).routes();
        new FilterPresetController(dbi).routes();


    }

    private static String env(String env, String defaultValue) {
        String value = System.getenv(env);
        return value == null ? defaultValue : value;
    }

    private static String env(String env) {
        String value = System.getenv(env);
        if (value == null) {
            System.err.println("Env var " + env + " must be set");
            System.exit(1);
        }
        return value;
    }


}
