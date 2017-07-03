package pandas.admin.marcexport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import freemarker.template.TemplateException;
import pandas.admin.core.Web;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class MarcExport {
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    PandasDAO dao;

    public MarcExport(PandasDAO dao) {
        this.dao = dao;
    }

    public void routes() {
        Spark.get("/admin/marcexport", this::get);
        Spark.post("/admin/marcexport", this::post);
        Spark.get("/admin/marcexport/MarcExport.js", Web.serveResource("/pandas/admin/marcexport/MarcExport.js"));
        Spark.get("/admin/marcexport/titles.json", this::getTitlesJson);
    }

    public String get(Request req, Response res) throws IOException, TemplateException {
        return Web.render(req, "pandas/admin/marcexport/MarcExport.ftl");
    }

    public Response post(Request req, Response res) {
        try (OutputFormat format = lookupFormat(res, req.queryParamOrDefault("format", "text"))) {
            String[] ids = req.queryParamOrDefault("ids", "").split("\\s+");
            for (String id : ids) {
                id = id.trim().replace("nla.arc-", "");
                if (!id.isEmpty()) {
                    long pi = Long.parseLong(id);
                    Title title = dao.findTitle(pi);
                    if (title == null) {
                        format.notFound(pi);
                        return res;
                    }
                    format.write(MarcMappings.nlaCatalogue(title, new Date()));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return res;
    }

    public String getTitlesJson(Request req, Response res) {
        Instant startTime = LocalDate.parse(req.queryParamOrDefault("startDate", LocalDate.now().minusDays(7).toString())).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endTime = LocalDate.parse(req.queryParamOrDefault("endDate", LocalDate.now().toString())).atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant();
        boolean includeMono = Boolean.parseBoolean(req.queryParamOrDefault("includeMono", "true"));
        boolean includeIntegrating = Boolean.parseBoolean(req.queryParamOrDefault("includeIntegrating", "true"));
        boolean includeSerial = Boolean.parseBoolean(req.queryParamOrDefault("includeSerial", "false"));
        boolean includeCataloguingNotRequired = Boolean.parseBoolean(req.queryParamOrDefault("includeCataloguingNotRequired", "false"));
        boolean includeCollectionMembers = Boolean.parseBoolean(req.queryParamOrDefault("includeCollectionMembers", "false"));

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
        res.type("application/json");
        return gson.toJson(m);
    }

    private static OutputFormat lookupFormat(Response res, String formatString) throws IOException {
        if ("text".equals(formatString)) {
            return new TextOutputFormat(res);
        } else if ("marc".equals(formatString)) {
            return new MarcOutputFormat(res);
        } else {
            throw new IllegalArgumentException("format must be text or marc, not: " + formatString);
        }
    }
}
