package pandas.admin;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import org.skife.jdbi.v2.DBI;
import pandas.PandasDbConfig;
import pandas.admin.gather.FilterPresetController;
import pandas.admin.marcexport.MarcExport;
import pandas.admin.marcexport.PandasDAO;

import javax.sql.DataSource;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.resource;

public class PandasAdmin {
    private final DataSource dataSource;

    public PandasAdmin(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static void main(String args[]) {

        DataSource dataSource = new PandasDbConfig(System.getenv()).open();

        int webPort = Integer.parseInt(env("PORT", "8080"));
        String webAddress = env("BIND_ADDRESS", "127.0.0.1");
        String contextPath = env("CONTEXT_PATH", "/admin");

        HttpHandler webapp = new PandasAdmin(dataSource).webapp();

        Undertow.builder().addHttpListener(webPort, webAddress)
                .setHandler(path()
                        .addExactPath("/", Handlers.redirect(contextPath + "/"))
                        .addPrefixPath(contextPath, webapp))
                .build().start();
    }

    public HttpHandler webapp() {
        DBI dbi = new DBI(dataSource);
        PandasDAO dao = dbi.onDemand(PandasDAO.class);

        ClassLoader classLoader = PandasAdmin.class.getClassLoader();
        RoutingHandler routes = Handlers.routing()
                .get("/", Handlers.redirect("marcexport"))
                .setFallbackHandler(path(resource(new ClassPathResourceManager(classLoader, "pandas/admin/static")))
                        .addPrefixPath("/webjars", resource(new ClassPathResourceManager(classLoader, "META-INF/resources/webjars")).setCacheTime(60 * 60 * 24 * 365)));

        new MarcExport(dao).routes(routes);
        new FilterPresetController(dbi).routes(routes);

        HttpHandler webapp = new BlockingHandler(routes);
        webapp = new EagerFormParsingHandler(webapp);
        return webapp;
    }

    private static String env(String env, String defaultValue) {
        String value = System.getenv(env);
        return value == null ? defaultValue : value;
    }
}
