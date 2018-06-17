package pandas.admin.gather;

import freemarker.template.TemplateException;
import io.undertow.Handlers;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.skife.jdbi.v2.DBI;
import pandas.admin.core.Web;

import java.io.IOException;

import static io.undertow.server.handlers.form.FormDataParser.FORM_DATA;

public class FilterPresetController {
    private FilterPresetDAO dao;

    public FilterPresetController(DBI dbi) {
        dao = dbi.onDemand(FilterPresetDAO.class);
    }

    public void routes(RoutingHandler webapp) {
        webapp.get("/gather/filterpresets", this::list);
        webapp.get("/gather/filterpresets/new", this::newForm);
        webapp.post("/gather/filterpresets/create", this::create);
        webapp.get("/gather/filterpresets/{id}", this::edit);
        webapp.post("/gather/filterpresets/{id}/update", this::update);
        webapp.post("/gather/filterpresets/{id}/delete", this::delete);
    }

    void newForm(HttpServerExchange http) throws IOException, TemplateException {
        Web.render(http, "/pandas/admin/gather/FilterPresetEdit.ftl",
                "preset", new FilterPreset());
    }

    void update(HttpServerExchange http) {
        FilterPreset preset = parseForm(http);
        if (dao.updateFilterPreset(preset) == 0) {
            notFound(http);
            return;
        }
        redirectToPresets(http);
    }

    private void redirectToPresets(HttpServerExchange http) {
        http.setStatusCode(StatusCodes.FOUND);
        http.getResponseHeaders().put(Headers.LOCATION, http.getResolvedPath() + "/gather/filterpresets");
        http.endExchange();
    }

    void create(HttpServerExchange http) {
        FilterPreset preset = parseForm(http);
        dao.insertFilterPreset(preset);
        redirectToPresets(http);
    }

    void delete(HttpServerExchange http) {
        int id = Integer.parseInt(Web.routeParam(http, "id"));
        if (dao.deleteFilterPreset(id) == 0) {
            notFound(http);
            return;
        }
        redirectToPresets(http);
    }

    private void notFound(HttpServerExchange http) {
        http.setStatusCode(StatusCodes.NOT_FOUND);
        http.getResponseSender().send("No such filter preset");
    }

    private FilterPreset parseForm(HttpServerExchange http) {
        FormData form = http.getAttachment(FORM_DATA);
        FilterPreset preset = new FilterPreset();
        String idString = Web.routeParam(http, "id");
        if (idString != null) {
            preset.setId(Long.parseLong(idString));
        }
        preset.setName(form.getFirst("name").getValue());
        preset.setFilters(form.getFirst("filters").getValue().replace("\n", " "));
        return preset;
    }


    void list(HttpServerExchange http) throws IOException, TemplateException {
        Web.render(http, "/pandas/admin/gather/FilterPresetList.ftl",
                "presets", dao.listFilterPresets());
    }

    void edit(HttpServerExchange http) throws IOException, TemplateException {
        int id = Integer.parseInt(Web.routeParam(http, "id"));
        FilterPreset preset = dao.findFilterPreset(id);
        if (preset == null) {
            notFound(http);
            return;
        }
        Web.render(http, "/pandas/admin/gather/FilterPresetEdit.ftl",
                "preset", preset);
    }

}
