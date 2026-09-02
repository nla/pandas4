package pandas.collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import pandas.IntegrationTest;
import pandas.agency.User;
import pandas.agency.UserService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TitleIntegrationTest extends IntegrationTest {
    @Autowired
    TitleService titleService;
    @Autowired
    TitleRepository titleRepository;
    @Autowired
    UserService userService;

    @Test
    @WithUserDetails("admin")
    public void testBulkAddFormWithoutCollection() throws Exception {
        mockMvc.perform(get("/titles/bulkadd"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bulk add websites")));
    }

    @Test
    @WithUserDetails("admin")
    @Transactional
    public void testBulkChange() throws Exception {
        var titleForm = titleService.newTitleForm(Collections.emptySet(), Collections.emptySet());
        titleForm.setSeedUrls("http://example.org/");
        titleForm.setName("Bulk Change Integration Test Title");
        User user = userService.getCurrentUser();
        Title title = titleService.save(titleForm, user);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();
        var bulkEditForm = new TitleBulkEditForm();
        bulkEditForm.setTitles(List.of(title));
        bulkEditForm.setOneoffDate(LocalDate.of(2022, 8, 30));
        bulkEditForm.setEditOneoffDate(true);
        titleService.bulkEdit(bulkEditForm, user);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();
        Title changedTitle = titleRepository.findById(title.getId()).orElseThrow();
        assertEquals(1, changedTitle.getGather().getOneoffDates().size());
    }
}
