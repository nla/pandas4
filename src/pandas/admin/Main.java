package pandas.admin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.skife.jdbi.v2.DBI;
import pandas.admin.gather.FilterPresetController;
import pandas.admin.marcexport.MarcExport;
import pandas.admin.marcexport.PandasDAO;
import spark.Spark;
import java.io.InputStream;

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


        SparkWorkaround.contextPath(env("CONTEXT_PATH", "/admin"));
        Spark.ipAddress(env("BIND_ADDRESS", "127.0.0.1"));
        Spark.port(Integer.parseInt(env("PORT", "3001")));

        Spark.staticFileLocation("pandas/admin/static");

        Spark.get("/webjars/*", (req, res) -> {
            String path = req.splat()[0];
            if (path.contains("/../")) {
                res.status(400);
                return null;
            }
            InputStream stream = Main.class.getResourceAsStream("/META-INF/resources/webjars/" + path);
            if (stream == null) {
                res.status(404);
                return "Not found";
            }
            if (path.endsWith(".css")) {
                res.type("text/css");
            } else if (path.endsWith(".js")) {
                res.type("application/javascript");
            } else if (path.endsWith(".woff2")) {
                res.type("application/font-woff2");
            } else if (path.endsWith(".woff2")) {
                res.type("application/font-woff");
            } else if (path.endsWith(".ttf")) {
                res.type("font/ttf");
            } else {
                res.status(500);
                return "Unhandled type";
            }
            res.header("Cache-Control", "max-age=31556926");
            return stream;
        });

        Spark.get("/", (req, res) -> {
           res.redirect(req.contextPath() + "/marcexport");
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
