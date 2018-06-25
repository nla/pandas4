package pandas.admin.marcexport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import freemarker.template.TemplateException;
import io.undertow.Handlers;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.util.Headers;
import pandas.admin.core.Web;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static io.undertow.server.handlers.form.FormDataParser.FORM_DATA;
import static pandas.admin.core.Web.firstOrDefault;

public class MarcExport {
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    PandasDAO dao;

    public MarcExport(PandasDAO dao) {
        this.dao = dao;
    }

    public void routes(RoutingHandler routing) {
        routing.get("/marcexport", this::get);
        routing.post("/marcexport", this::post);
        routing.get("/marcexport/MarcExport.js", Handlers.resource(new ClassPathResourceManager(getClass().getClassLoader(), "pandas/admin/")));
        routing.get("/marcexport/titles.json", this::getTitlesJson);
    }

    void get(HttpServerExchange ex) throws IOException, TemplateException {
        Web.render(ex, "pandas/admin/marcexport/MarcExport.ftl");
    }

    void post(HttpServerExchange http) {
        FormData formData = http.getAttachment(FORM_DATA);
        String[] ids = formData.getFirst("ids").getValue().split("\\s+");
        try (OutputFormat format = lookupFormat(http, firstOrDefault(http.getQueryParameters(), "format", "text"))) {
            for (String id : ids) {
                id = id.trim().replace("nla.arc-", "");
                if (!id.isEmpty()) {
                    long pi = Long.parseLong(id);
                    Title title = dao.findTitle(pi);
                    if (title == null) {
                        format.notFound(pi);
                    } else {
                        format.write(MarcMappings.nlaCatalogue(title, new Date()));
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void getTitlesJson(HttpServerExchange http) {
        Map<String, Deque<String>> q = http.getQueryParameters();
        Instant startTime = LocalDate.parse(firstOrDefault(q, "startDate", LocalDate.now().minusDays(7).toString())).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endTime = LocalDate.parse(firstOrDefault(q, "endDate", LocalDate.now().toString())).atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant();
        boolean includeMono = Boolean.parseBoolean(firstOrDefault(q, "includeMono", "true"));
        boolean includeIntegrating = Boolean.parseBoolean(firstOrDefault(q, "includeIntegrating", "true"));
        boolean includeSerial = Boolean.parseBoolean(firstOrDefault(q, "includeSerial", "false"));
        boolean includeCataloguingNotRequired = Boolean.parseBoolean(firstOrDefault(q, "includeCataloguingNotRequired", "false"));
        boolean includeCollectionMembers = Boolean.parseBoolean(firstOrDefault(q, "includeCollectionMembers", "false"));

        List<TitleSummaryRow> titleSummaryRows = dao.listTitles(Date.from(startTime), Date.from(endTime), includeMono, includeIntegrating, includeSerial, includeCataloguingNotRequired, includeCollectionMembers);

        // We filter out the other agencies here because if we add an agency constraint to the SQL
        // Oracle's query planner is switches to scanning TITLE rather than scanning STATUS_HISTORY which is
        // much slower for and I can't figure out how to hint it not to.
        List<TitleSummaryRow> filtered = new ArrayList<TitleSummaryRow>();
        for (TitleSummaryRow row : titleSummaryRows) {
            if (row.getAgencyId() == 1) {
                filtered.add(row);
            }
        }

        Map<String,Object> m = new HashMap<>();
        m.put("data", filtered);
        http.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        http.getResponseSender().send(gson.toJson(m));
     }

    private static OutputFormat lookupFormat(HttpServerExchange http, String formatString) throws IOException {
        if ("text".equals(formatString)) {
            return new TextOutputFormat(http);
        } else if ("marc".equals(formatString)) {
            return new MarcOutputFormat(http);
        } else {
            throw new IllegalArgumentException("format must be text or marc, not: " + formatString);
        }
    }
}
