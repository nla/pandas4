package pandas.report;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import pandas.agency.AgencyRepository;
import pandas.agency.User;
import pandas.agency.UserService;
import pandas.collection.PublisherTypeRepository;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Serves the on-demand reports. Reports are plain Spring components implementing {@link ReportDefinition};
 * this controller collects them into a slug registry and renders any of them to HTML or CSV from a
 * single {@link ReportView} produced by one query pass.
 */
@Controller
public class ReportController {
    private final Map<String, ReportDefinition> reports = new LinkedHashMap<>();
    private final AgencyRepository agencyRepository;
    private final PublisherTypeRepository publisherTypeRepository;
    private final UserService userService;

    public ReportController(List<ReportDefinition> reports, AgencyRepository agencyRepository,
                            PublisherTypeRepository publisherTypeRepository, UserService userService) {
        reports.stream()
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .forEach(r -> this.reports.put(r.slug(), r));
        this.agencyRepository = agencyRepository;
        this.publisherTypeRepository = publisherTypeRepository;
        this.userService = userService;
    }

    @GetMapping("/reports")
    public String index(Model model) {
        model.addAttribute("reports", reports.values());
        return "ReportIndex";
    }

    @GetMapping("/reports/{slug}")
    public String view(@PathVariable String slug, Model model,
                       @RequestParam(required = false) String agency,
                       @RequestParam(required = false) LocalDate startDate,
                       @RequestParam(required = false) LocalDate endDate,
                       @RequestParam(required = false, defaultValue = "false") boolean details,
                       @RequestParam(required = false) Long publisherType,
                       @RequestParam(required = false) Long restrictionType) {
        ReportDefinition report = report(slug);
        ReportParams params = new ReportParams(resolveAgency(agency), startDate, endDate, details, publisherType, restrictionType);
        model.addAttribute("report", report);
        model.addAttribute("params", params);
        model.addAttribute("view", report.generate(params));
        model.addAttribute("agencies", agencyRepository.findAllOrdered());
        model.addAttribute("publisherTypes", publisherTypeRepository.findAll());
        return "ReportView";
    }

    @GetMapping(value = "/reports/{slug}.csv", produces = "text/csv")
    public void csv(@PathVariable String slug, HttpServletResponse response,
                    @RequestParam(required = false) String agency,
                    @RequestParam(required = false) LocalDate startDate,
                    @RequestParam(required = false) LocalDate endDate,
                    @RequestParam(required = false, defaultValue = "false") boolean details,
                    @RequestParam(required = false) Long publisherType,
                    @RequestParam(required = false) Long restrictionType) throws IOException {
        ReportDefinition report = report(slug);
        ReportParams params = new ReportParams(resolveAgency(agency), startDate, endDate, details, publisherType, restrictionType);
        ReportView view = report.generate(params);
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.builder("attachment").filename(slug + ".csv").build().toString());
        writeCsv(view, response);
    }

    /**
     * Resolves the agency request parameter to an agency id. A missing parameter (i.e. a fresh visit
     * to a report) defaults to the current user's agency; an explicit empty value means "all agencies".
     */
    private Long resolveAgency(String agency) {
        if (agency == null) {
            User user = userService.getCurrentUser();
            return user != null && user.getAgency() != null ? user.getAgency().getId() : null;
        }
        return agency.isBlank() ? null : Long.parseLong(agency);
    }

    private ReportDefinition report(String slug) {
        ReportDefinition report = reports.get(slug);
        if (report == null) throw new ResponseStatusException(NOT_FOUND, "No such report: " + slug);
        return report;
    }

    private static void writeCsv(ReportView view, HttpServletResponse response) throws IOException {
        boolean multiSection = view.sections().size() > 1;
        try (CSVPrinter csv = CSVFormat.DEFAULT.print(new OutputStreamWriter(response.getOutputStream(), UTF_8))) {
            for (Section section : view.sections()) {
                for (Table table : section.tables()) {
                    List<String> header = new ArrayList<>();
                    if (multiSection) header.add("Section");
                    header.addAll(table.columns());
                    csv.printRecord(header);
                    for (Row row : table.rows()) {
                        List<String> record = new ArrayList<>();
                        if (multiSection) record.add(section.title());
                        for (Cell cell : row.cells()) record.add(cell.csv());
                        csv.printRecord(record);
                    }
                }
            }
        }
    }
}
