package pandas.report;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.test.context.support.WithUserDetails;
import pandas.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportsIntegrationTest extends IntegrationTest {

    /**
     * Performs a request to establish an HTTP session and returns its SESSION cookie. Report pages
     * render the sidebar's logout form, whose deferred CSRF token lazily creates the (Spring Session)
     * session as it loads during rendering. MockMvc's small response buffer can commit the response
     * before then, and Spring Session refuses to create a session after the response is committed.
     * A real browser already carries a session, so this only bites under MockMvc — priming one here
     * reproduces that: with an existing session the CSRF token has no session to create mid-render.
     */
    private Cookie session() throws Exception {
        return mockMvc.perform(get("/reports")).andReturn().getResponse().getCookie("SESSION");
    }

    @Test
    @WithUserDetails("admin")
    void indexListsReports() throws Exception {
        mockMvc.perform(get("/reports").cookie(session()))
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
        Cookie session = session();
        mockMvc.perform(get("/reports/" + slug).cookie(session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));

        mockMvc.perform(get("/reports/" + slug + ".csv").cookie(session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    @Test
    @WithUserDetails("admin")
    void unknownReportReturns404() throws Exception {
        mockMvc.perform(get("/reports/no-such-report")).andExpect(status().isNotFound());
    }
}
