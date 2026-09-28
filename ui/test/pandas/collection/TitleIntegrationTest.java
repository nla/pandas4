package pandas.collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import pandas.IntegrationTest;
import pandas.agency.User;
import pandas.agency.UserService;
import pandas.core.PandasUserDetailsService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TitleIntegrationTest extends IntegrationTest {
    @Autowired
    TitleService titleService;
    @Autowired
    TitleRepository titleRepository;
    @Autowired
    UserService userService;
    @Autowired
    PublisherTypeRepository publisherTypeRepository;

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
    public void infoUserBulkAddCreatesNominations() throws Exception {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                authentication.getPrincipal(), authentication.getCredentials(),
                PandasUserDetailsService.authoritiesForRoleType("infouser")));
        User currentUser = userService.getCurrentUser();
        PublisherType publisherType = publisherTypeRepository.findAll().iterator().next();

        mockMvc.perform(post("/titles/bulkadd")
                        .with(csrf())
                        .param("url", "https://bulk-nomination.example.org/")
                        .param("name", "Bulk nomination")
                        .param("publisherName", "Bulk nomination publisher")
                        .param("publisherType", publisherType.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/titles?id=*"));

        Title title = titleRepository.findByTitleUrlIn(List.of("https://bulk-nomination.example.org/")).get(0);
        assertEquals(Status.NOMINATED, title.getStatus());
        assertEquals(currentUser, title.getNominator());
        assertFalse(title.getOwnerHistories().isEmpty());
        assertEquals("Nominated new title", title.getOwnerHistories().get(0).getNote());
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
