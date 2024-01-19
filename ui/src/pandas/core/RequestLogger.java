package pandas.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.BaseSessionEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.Principal;
import java.util.Arrays;
import java.util.function.Supplier;

@Component
public class RequestLogger {
    static ThreadLocal<Context> context = ThreadLocal.withInitial(Context::new);
    private static final long DEFAULT_SLOW_QUERY_NANOS = 25 * 1000 * 1000; // 25 ms
    private static final int EXTRA_LINES_MAX_LENGTH = 16 * 1024;

    static RequestLogger instance;

    @PersistenceContext
    EntityManager entityManager;

    public RequestLogger() {
        instance = this;
    }

    public static class HibernateSessionEventListener extends BaseSessionEventListener {
    }

    public static class Context {
        public String sql;
        long startTime;
        long dbTimeNanos;
        long queries;
        long rows;
        long slowQueryNanos;
        StringBuilder extraLines = new StringBuilder();

        public void beforeRequest(HttpServletRequest request, HttpServletResponse response) {
            startTime = System.currentTimeMillis();
            queries = 0;
            dbTimeNanos = 0;
            rows = 0;
            String slowQueryNanosParam = request.getParameter("slowQueryNanos");
            slowQueryNanos = slowQueryNanosParam == null ? DEFAULT_SLOW_QUERY_NANOS : Long.parseLong(slowQueryNanosParam);
            extraLines.setLength(0);
        }

        public void afterRequest(HttpServletRequest request, HttpServletResponse response, Exception ex) {
            if (request.getRequestURI().contains("/assets/")) return; // don't both logging static files
            long elapsed = System.currentTimeMillis() - startTime;
            Principal principal = request.getUserPrincipal();
            String requestUrl = request.getRequestURI();
            if (request.getQueryString() != null) {
                requestUrl += "?" + request.getQueryString();
            }
            System.out.println("time=" + elapsed + "ms" +
                    " dbtime=" + dbTimeNanos / 1000000 + "ms" +
                    " queries=" + queries +
                    " rows=" + rows +
                    " user=" + (principal == null ? "" : principal.getName()) +
                    " " + request.getMethod() + " " + requestUrl + extraLines.toString());
        }

        public void afterSqlExecute(Supplier<String> sql, Supplier<String> hql, long timeElapsedNanos, long rows) {
            queries++;
            dbTimeNanos += timeElapsedNanos;
            this.rows += rows;
            if (timeElapsedNanos > slowQueryNanos && extraLines.length() < EXTRA_LINES_MAX_LENGTH) {
                extraLines.append("\n    SLOW (").append(timeElapsedNanos / 1000 / 1000).append("ms): ")
                        .append("[").append(rows).append(" rows] ")
                        .append(sql.get());

                String hqlString = hql.get();
                if (hqlString != null && !hqlString.isBlank() && !hqlString.startsWith("[CRITERIA]")) {
                    extraLines.append("\n         HQL: ").append(hqlString.replace('\n', ' '));
                }

                var frame = Arrays.stream(Thread.currentThread().getStackTrace())
                        .filter(e -> e.getClassName().startsWith("pandas.") && !e.getClassName().startsWith("pandas.core."))
                        .findFirst()
                        .map(StackTraceElement::toString)
                        .orElse(null);
                if (frame != null) {
                    extraLines.append("\n         via ").append(frame);
                }
            }
        }
    }

    @Bean
    public WebMvcConfigurer mvcConfig() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor());
            }
        };
    }

    public HandlerInterceptor interceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                // ((SessionImpl)entityManager.getDelegate()).getSessionFactory().getServiceRegistry().getService(JdbcServices.class)
                context.get().beforeRequest(request, response);
                return true;
            }

            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
                context.get().afterRequest(request, response, ex);
            }
        };
    }

}
