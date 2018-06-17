package pandas.admin.core;

import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import pandas.admin.PandasAdmin;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Web {
    private static Configuration freemarker;

    static {
        freemarker = new Configuration(Configuration.VERSION_2_3_26);
        freemarker.setDefaultEncoding("UTF-8");
        freemarker.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        freemarker.setClassForTemplateLoading(PandasAdmin.class, "/");
        freemarker.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
        freemarker.setOutputFormat(HTMLOutputFormat.INSTANCE);
    }

    public static void render(HttpServerExchange http, String view, Object... modelKeysAndValues) throws IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();
        model.put("http", http);
        for (int i = 0; i < modelKeysAndValues.length; i += 2) {
            model.put((String)modelKeysAndValues[i], modelKeysAndValues[i + 1]);
        }
        StringWriter buffer = new StringWriter();
        Template template = freemarker.getTemplate(view);
        template.process(model, buffer);
        http.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
        http.getResponseSender().send(buffer.toString());
    }

    public static String firstOrDefault(Map<String, Deque<String>> map, String param, String defaultValue) {
        Deque<String> deque = map.get(param);
        if (deque == null) {
            return defaultValue;
        }
        return deque.getFirst();
    }

    public static String routeParam(HttpServerExchange http, String param) {
        return http.getAttachment(PathTemplateMatch.ATTACHMENT_KEY).getParameters().get(param);
    }
}
