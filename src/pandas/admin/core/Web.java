package pandas.admin.core;

import freemarker.core.HTMLOutputFormat;
import freemarker.core.OutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import pandas.admin.Main;
import spark.Request;
import spark.Route;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Web {
    private static Configuration freemarker;

    static {
        freemarker = new Configuration(Configuration.VERSION_2_3_26);
        freemarker.setDefaultEncoding("UTF-8");
        freemarker.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        freemarker.setClassForTemplateLoading(Main.class, "/");
        freemarker.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
        freemarker.setOutputFormat(HTMLOutputFormat.INSTANCE);
    }

    public static String render(Request req, String view, Object... modelKeysAndValues) throws IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();
        model.put("request", req);
        for (int i = 0; i < modelKeysAndValues.length; i += 2) {
            model.put((String)modelKeysAndValues[i], modelKeysAndValues[i + 1]);
        }
        StringWriter buffer = new StringWriter();
        Template template = freemarker.getTemplate(view);
        template.process(model, buffer);
        return buffer.toString();
    }

    public static Route serveResource(String resource) {
        URL url = Main.class.getResource(resource);
        if (url == null) {
            throw new RuntimeException("Couldn't find resource on classpath: " + resource);
        }
        String contentType;
        if (resource.endsWith(".js")) {
            contentType = "application/javascript";
        } else {
            throw new RuntimeException("Don't recognise file-extension for: " + resource);
        }
        return (req, res) -> {
            res.type(contentType);
            try (InputStream in = url.openStream();
                 OutputStream out = res.raw().getOutputStream()) {
                byte[] buf = new byte[16384];
                while (true) {
                    int n = in.read(buf);
                    if (n == -1) {
                        break;
                    }
                    out.write(buf, 0, n);
                }
                return 200;
            }
        };
    }
}
