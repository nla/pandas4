package pandas.report;

import org.springframework.stereotype.Component;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared helpers for the per-agency reports: the set of agencies to report on (ordered by name,
 * with the configured excluded agencies removed). Reports run one set-based query grouped by
 * {@code agency_id} and bucket the rows into these agencies in order, avoiding the per-agency and
 * per-cell N+1 queries of the PANDAS 3 engine.
 */
@Component
public class ReportSupport {
    private final AgencyRepository agencyRepository;
    private final ReportConfig config;

    public ReportSupport(AgencyRepository agencyRepository, ReportConfig config) {
        this.agencyRepository = agencyRepository;
        this.config = config;
    }

    /**
     * Ordered map of agency id to name for the reportable agencies. If {@code only} is non-null the map
     * is restricted to that single agency (still excluding the configured agencies).
     */
    public Map<Long, String> agencies(Long only) {
        Map<Long, String> result = new LinkedHashMap<>();
        for (Agency agency : agencyRepository.findAllOrdered()) {
            if (config.isExcluded(agency.getId())) continue;
            if (only != null && !only.equals(agency.getId())) continue;
            result.put(agency.getId(), agency.getOrganisation().getName());
        }
        return result;
    }
}
