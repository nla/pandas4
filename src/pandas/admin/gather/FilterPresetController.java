package pandas.admin.gather;

import freemarker.template.TemplateException;
import org.skife.jdbi.v2.DBI;
import pandas.admin.core.Web;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.IOException;

public class FilterPresetController {
    FilterPresetDAO dao;

    public FilterPresetController(DBI dbi) {
        dao = dbi.onDemand(FilterPresetDAO.class);
    }

    public void routes() {
        Spark.get("/admin/gather/filterpresets", this::list);
        Spark.get("/admin/gather/filterpresets/new", this::newForm);
        Spark.post("/admin/gather/filterpresets/create", this::create);
        Spark.get("/admin/gather/filterpresets/:id", this::edit);
        Spark.post("/admin/gather/filterpresets/:id/update", this::update);
        Spark.post("/admin/gather/filterpresets/:id/delete", this::delete);
    }

    String newForm(Request req, Response response) throws IOException, TemplateException {
        return Web.render(req, "/pandas/admin/gather/FilterPresetEdit.ftl",
                "preset", new FilterPreset());
    }

    String update(Request req, Response res) {
        FilterPreset preset = parseForm(req);
        if (dao.updateFilterPreset(preset) == 0) {
            return notFound(res);
        }
        res.redirect("/admin/gather/filterpresets", 302);
        return null;
    }

    String create(Request req, Response res) {
        FilterPreset preset = parseForm(req);
        dao.insertFilterPreset(preset);
        res.redirect("/admin/gather/filterpresets", 302);
        return null;
    }

    String delete(Request req, Response res) {
        int id = Integer.parseInt(req.params(":id"));
        if (dao.deleteFilterPreset(id) == 0) {
            return notFound(res);
        }
        res.redirect("/admin/gather/filterpresets", 302);
        return null;
    }

    private String notFound(Response res) {
        res.status(404);
        return "No such filter preset";
    }

    private FilterPreset parseForm(Request req) {
        FilterPreset preset = new FilterPreset();
        String idString = req.params(":id");
        if (idString != null) {
            preset.setId(Long.parseLong(idString));
        }
        preset.setName(req.queryParams("name"));
        preset.setFilters(req.queryParams("filters").replace("\n", " "));
        return preset;
    }


    String list(Request req, Response res) throws IOException, TemplateException {
        return Web.render(req, "/pandas/admin/gather/FilterPresetList.ftl",
                "presets", dao.listFilterPresets());
    }

    String edit(Request req, Response res) throws IOException, TemplateException {
        int id = Integer.parseInt(req.params(":id"));
        FilterPreset preset = dao.findFilterPreset(id);
        if (preset == null) {
            return notFound(res);
        }
        return Web.render(req, "/pandas/admin/gather/FilterPresetEdit.ftl",
                "preset", preset);
    }

}
