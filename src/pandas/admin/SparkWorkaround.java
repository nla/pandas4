package pandas.admin;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.ThreadPool;
import spark.embeddedserver.EmbeddedServer;
import spark.embeddedserver.EmbeddedServers;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;
import spark.embeddedserver.jetty.EmbeddedJettyServer;
import spark.embeddedserver.jetty.JettyHandler;
import spark.embeddedserver.jetty.JettyServerFactory;
import spark.http.matching.MatcherFilter;
import spark.route.Routes;
import spark.staticfiles.StaticFilesConfiguration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SparkWorkaround {
    /**
     * https://github.com/perwendel/spark/issues/888#issuecomment-345990436
     */
    static void contextPath(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {

            return;
        }
        EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, new EmbeddedJettyFactory() {
            @Override
            public EmbeddedServer create(Routes routeMatcher, StaticFilesConfiguration staticFilesConfiguration, boolean hasMultipleHandler) {
                // Spark uses the full ServletRequest.getRequestURI() rather than a context relative path like
                // getPathInfo as the path for routing so we must wrap the request and strip the context path
                // off the front of it.
                MatcherFilter matcherFilter = new MatcherFilter(routeMatcher, staticFilesConfiguration, false, hasMultipleHandler);
                matcherFilter.init(null);
                JettyHandler handler = new JettyHandler(matcherFilter) {
                    @Override
                    public void doHandle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                        request = new HttpServletRequestWrapper(request) {
                            @Override
                            public String getRequestURI() {
                                return super.getRequestURI().substring(path.length());
                            }
                        };
                        super.doHandle(target, baseRequest, request, response);
                    }
                };
                ServletContextHandler servletContextHandler = new ServletContextHandler();
                servletContextHandler.setContextPath(path);
                servletContextHandler.setHandler(handler);

                // For convenience also install a redirect from / to the context path
                HandlerWrapper wrapper = new HandlerWrapper() {
                    @Override
                    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                        if (target.equals("/")) {
                            response.sendRedirect(path);
                        } else {
                            super.handle(target, baseRequest, request, response);
                        }
                    }
                };
                wrapper.setHandler(servletContextHandler);

                // unfortunately JettyServer is package-private so we have to duplicate it
                return new EmbeddedJettyServer(new JettyServerFactory() {
                    @Override
                    public Server create(int i, int j, int k) {
                        // XXX: we're ignoring the the thread limits
                        return new Server();
                    }

                    @Override
                    public Server create(ThreadPool threadPool) {
                        return new Server(threadPool);
                    }
                }, wrapper);
            }
        });
    }
}
