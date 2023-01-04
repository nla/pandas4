package pandas;

import com.p6spy.engine.common.ResultSetInformation;
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.JdbcEventListener;
import com.p6spy.engine.event.SimpleJdbcEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeliveryRequestLogger {
    private ThreadLocal<Context> context = ThreadLocal.withInitial(Context::new);
    private static final long SLOW_QUERY_NANOS = 25*1000*1000; // 25 ms
    private static final int EXTRA_LINES_MAX_LENGTH = 16 * 1024;

    /**
     * Detailed query logging when a single java method generates more than the given number of queries.
     */
    public static int logExcessiveQueries = 0;

    public static class Context {
        long startTime;
        long dbTimeNanos;
        long queries;
        long rows;
        StringBuilder extraLines = new StringBuilder();
        Map<String, Long> traces = new HashMap<>();
        Map<String, List<String>> traceSql = new HashMap<>();

        public void beforeRequest(HttpServletRequest request, HttpServletResponse response) {
            startTime = System.currentTimeMillis();
            queries = 0;
            dbTimeNanos = 0;
            rows = 0;
            extraLines.setLength(0);
            if (logExcessiveQueries > 0) {
                traces = new HashMap<>();
                traceSql = new HashMap<>();
            }
        }

        public void afterRequest(HttpServletRequest request, HttpServletResponse response, Exception ex) {
            if (request.getRequestURI().contains("/assets/")) return; // don't both logging static files
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("time=" + elapsed + "ms" +
                    " dbtime=" + dbTimeNanos / 1000000 + "ms" +
                    " queries=" + queries +
                    " rows=" + rows +
                    " ip=" + request.getRemoteAddr() +
                    " " + request.getMethod() + " " + request.getRequestURI() + extraLines.toString());

            if (logExcessiveQueries > 0) {
                for (var entry : traces.entrySet()) {
                    if (entry.getValue() > logExcessiveQueries) {
                        System.out.println(entry.getValue() + " " + entry.getKey().substring(100) + " ");
                        int i = 0;
                        for (String sql : traceSql.get(entry.getKey())) {
                            System.out.println("SQL" + i + " " + sql);
                            i++;
                            if (i > 10) break;
                        }
                    }
                }
            }
        }

        private static String guesstimateSourceLocation() {
            for (var entry : Thread.currentThread().getStackTrace()) {
                String className = entry.getClassName();
                if (className.startsWith("pandas.") && !className.contains("RequestLogger")) {
                    return entry.toString();
                }
            }
            return "";
        }

        public void afterSqlExecute(StatementInformation statementInformation, long timeElapsedNanos, SQLException e) {
            queries++;
            dbTimeNanos += timeElapsedNanos;
            if (timeElapsedNanos > SLOW_QUERY_NANOS && extraLines.length() < EXTRA_LINES_MAX_LENGTH) {
                extraLines.append("\n    SLOW (").append(timeElapsedNanos / 1000 / 1000).append("ms): ")
                        .append(guesstimateSourceLocation()).append(" ")
                        .append(statementInformation.getSqlWithValues());
            }

            if (logExcessiveQueries > 0) {
                var sb = new StringBuilder();
                for (var element : Thread.currentThread().getStackTrace()) {
                    sb.append(element.toString()).append("\n");
                }
                var stacktrace = sb.toString();
                long count = traces.getOrDefault(stacktrace, 0L);
                count += 1;
                traces.put(stacktrace, count);

                var list = traceSql.computeIfAbsent(stacktrace, k -> new ArrayList<>());
                list.add(statementInformation.getSqlWithValues());
            }
        }

        public void afterResultSetNext(ResultSetInformation resultSetInformation, long timeElapsedNanos, boolean hasNext, SQLException e) {
            dbTimeNanos += timeElapsedNanos;
            if (hasNext) {
                rows++;
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

    @Bean
    public JdbcEventListener myListener() {
        return new SimpleJdbcEventListener() {
            @Override
            public void onAfterAnyExecute(StatementInformation statementInformation, long timeElapsedNanos, SQLException e) {
                context.get().afterSqlExecute(statementInformation, timeElapsedNanos, e);
            }

            @Override
            public void onAfterResultSetNext(ResultSetInformation resultSetInformation, long timeElapsedNanos, boolean hasNext, SQLException e) {
                context.get().afterResultSetNext(resultSetInformation, timeElapsedNanos, hasNext, e);
            }
        };
    }

}
