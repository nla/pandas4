package pandas.marcexport;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Controller
public class MarcExportController {
    private final ExportTitleRepository exportTitleRepository;

    public MarcExportController(ExportTitleRepository exportTitleRepository) {
        this.exportTitleRepository = exportTitleRepository;
    }

    @GetMapping("/marcexport")
    String get() {
        return "MarcExport";
    }

    @PostMapping("/marcexport")
    String post(@RequestParam("ids") String idsParam, @RequestParam(value = "format", defaultValue = "text") String formatName,
              HttpServletResponse response) throws IOException {
        String[] ids = idsParam.split("\\s+");
        try (OutputFormat format = lookupFormat(response, formatName)) {
            for (String id : ids) {
                id = id.trim().replace("nla.arc-", "");
                if (!id.isEmpty()) {
                    long pi = Long.parseLong(id);
                    Title title = exportTitleRepository.findTitle(pi);
                    if (title == null) {
                        format.notFound(pi);
                    } else {
                        format.write(MarcMappings.nlaCatalogue(title, new Date()));
                    }
                }
            }
            format.close();
            return "";
        }
    }

    @GetMapping(value = "/marcexport/titles.json", produces = "application/json")
    @ResponseBody
    public Map<String, Object> getTitlesJson(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso=DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso=DATE) LocalDate endDate,
            @RequestParam(value = "includeMono", defaultValue = "true") boolean includeMono,
            @RequestParam(value = "includeIntegrating", defaultValue = "true") boolean includeIntegrating,
            @RequestParam(value = "includeSerial", defaultValue = "false") boolean includeSerial,
            @RequestParam(value = "includeCataloguingNotRequired", defaultValue = "false") boolean includeCataloguingNotRequired,
            @RequestParam(value = "includeCollectionMembers", defaultValue = "false") boolean includeCollectionMembers) {
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        if (endDate == null) endDate = LocalDate.now();
        Instant startTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endTime = endDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant();
        List<TitleSummary> titleSummaries = exportTitleRepository.listSummaries(Date.from(startTime), Date.from(endTime), includeMono, includeIntegrating, includeSerial, includeCataloguingNotRequired, includeCollectionMembers);

        // We filter out the other agencies here because if we add an agency constraint to the SQL
        // Oracle's query planner is switches to scanning TITLE rather than scanning STATUS_HISTORY which is
        // much slower for and I can't figure out how to hint it not to.
        List<TitleSummary> filtered = new ArrayList<>();
        for (TitleSummary row : titleSummaries) {
            if (row.getAgencyId() == 1) {
                filtered.add(row);
            }
        }

        Map<String,Object> m = new HashMap<>();
        m.put("data", filtered);
        return m;
     }

    private static OutputFormat lookupFormat(HttpServletResponse response, String formatString) throws IOException {
        if ("text".equals(formatString)) {
            return new TextOutputFormat(response);
        } else if ("marc".equals(formatString)) {
            return new MarcOutputFormat(response);
        } else {
            throw new IllegalArgumentException("format must be text or marc, not: " + formatString);
        }
    }
}
