package pandas.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.test.context.support.WithUserDetails;
import pandas.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportsIntegrationTest extends IntegrationTest {

    @Test
    @WithUserDetails("admin")
    void indexListsReports() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Total Archived Titles")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "total-archived-titles",
            "statistics-by-status",
            "scheduled-gathers",
            "newly-archived-titles",
            "titles-by-publisher-type",
            "legal-deposit",
    })
    @WithUserDetails("admin")
    void reportRendersAsHtmlAndCsv(String slug) throws Exception {
        mockMvc.perform(get("/reports/" + slug))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));

        mockMvc.perform(get("/reports/" + slug + ".csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    @Test
    @WithUserDetails("admin")
    void unknownReportReturns404() throws Exception {
        mockMvc.perform(get("/reports/no-such-report")).andExpect(status().isNotFound());
    }
}
