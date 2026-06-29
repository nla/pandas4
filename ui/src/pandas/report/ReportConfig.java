package pandas.report;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the reports. Replaces values that were hardcoded in the PANDAS 3 report engine
 * (the excluded agency ids and the pre-PANDAS legacy baseline) with externalised properties.
 */
@Configuration
@ConfigurationProperties(prefix = "pandas.reports")
public class ReportConfig {
    /**
     * Agencies excluded from all reports because they are not part of the PANDORA Archive proper
     * (historically: 3 = PADI Safekeeping, 4 = Independent Researchers, 13 = State Library of Tasmania).
     */
    private List<Long> excludedAgencies = new ArrayList<>();

    /** Files archived before PANDAS, added to the whole-of-archive totals in the Total Archived Titles report. */
    private long legacyFiles = 0;

    /** Bytes archived before PANDAS, added to the whole-of-archive totals in the Total Archived Titles report. */
    private long legacyBytes = 0;

    public List<Long> getExcludedAgencies() {
        return excludedAgencies;
    }

    public void setExcludedAgencies(List<Long> excludedAgencies) {
        this.excludedAgencies = excludedAgencies;
    }

    public boolean isExcluded(Long agencyId) {
        return agencyId != null && excludedAgencies.contains(agencyId);
    }

    public long getLegacyFiles() {
        return legacyFiles;
    }

    public void setLegacyFiles(long legacyFiles) {
        this.legacyFiles = legacyFiles;
    }

    public long getLegacyBytes() {
        return legacyBytes;
    }

    public void setLegacyBytes(long legacyBytes) {
        this.legacyBytes = legacyBytes;
    }
}
