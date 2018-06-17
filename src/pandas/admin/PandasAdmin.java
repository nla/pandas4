package pandas.admin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import org.skife.jdbi.v2.DBI;
import pandas.admin.gather.FilterPresetController;
import pandas.admin.marcexport.MarcExport;
import pandas.admin.marcexport.PandasDAO;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.resource;

public class PandasAdmin {

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

        int webPort = Integer.parseInt(env("PORT", "8080"));
        String webAddress = env("BIND_ADDRESS", "127.0.0.1");
        String contextPath = env("CONTEXT_PATH", "/admin");

        ClassLoader classLoader = PandasAdmin.class.getClassLoader();
        RoutingHandler routes = Handlers.routing()
                .get("/", Handlers.redirect("marcexport"))
                .setFallbackHandler(path(resource(new ClassPathResourceManager(classLoader, "pandas/admin/static")))
                        .addPrefixPath("/webjars", resource(new ClassPathResourceManager(classLoader, "META-INF/resources/webjars")).setCacheTime(60 * 60 * 24 * 365)));

        new MarcExport(dao).routes(routes);
        new FilterPresetController(dbi).routes(routes);

        HttpHandler webapp = new BlockingHandler(routes);
        webapp = new EagerFormParsingHandler(webapp);

        Undertow.builder().addHttpListener(webPort, webAddress)
                .setHandler(path()
                        .addExactPath("/", Handlers.redirect(contextPath + "/"))
                        .addPrefixPath(contextPath, webapp))
                .build().start();
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
