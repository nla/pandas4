package pandas.gather;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import pandas.IntegrationTest;
import pandas.agency.UserService;
import pandas.collection.Title;
import pandas.collection.TitleSearcher;
import pandas.collection.TitleService;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InstanceIntegrationTest extends IntegrationTest {
    @Autowired TitleService titleService;
    @Autowired InstanceService instanceService;
    @Autowired UserService userService;
    @Autowired StateRepository stateRepository;
    @Autowired InstanceRepository instanceRepository;
    @Autowired TitleSearcher titleSearcher;

    @Test
    @WithUserDetails("admin")
    @Transactional
    public void testFirstGatherDateSearchIsDerivedFromInstances() {
        Title title = createTitle();
        instanceService.createInstance(GatherMethod.HERITRIX, title);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        var params = new LinkedMultiValueMap<String, String>();
        params.set("firstgather.start", LocalDate.now().minusDays(1).toString());
        params.set("firstgather.end", LocalDate.now().plusDays(1).toString());

        TestTransaction.start();
        var results = titleSearcher.search(params, PageRequest.ofSize(100));
        assertTrue(results.stream().anyMatch(result -> result.getId().equals(title.getId())));
    }

    @Test
    @WithUserDetails("admin")
    public void testArchiveSelected() throws Exception {
        Title title = createTitle();
        Instance instance1 = instanceService.createInstance(GatherMethod.HERITRIX, title);
        Instance instance2 = instanceService.createInstance(GatherMethod.HERITRIX, title);
        instanceService.updateState(instance1.getId(), State.GATHERED);
        instanceService.updateState(instance2.getId(), State.GATHERED);

        mockMvc.perform(post("/instances/archive")
                .with(csrf())
                .param("instance", String.valueOf(instance1.getId()), String.valueOf(instance2.getId())))
                .andExpect(status().is3xxRedirection());

        assertEquals(State.ARCHIVING, instanceService.refresh(instance1).getState());
        assertEquals(State.ARCHIVING, instanceService.refresh(instance2).getState());
    }

    @Test
    @WithUserDetails("admin")
    public void testDeleteSelected() throws Exception {
        Title title = createTitle();
        Instance instance1 = instanceService.createInstance(GatherMethod.HERITRIX, title);
        Instance instance2 = instanceService.createInstance(GatherMethod.HERITRIX, title);

        mockMvc.perform(post("/instances/delete")
                .with(csrf())
                .param("instance", String.valueOf(instance1.getId()), String.valueOf(instance2.getId())))
                .andExpect(status().is3xxRedirection());

        assertEquals(State.DELETING, instanceService.refresh(instance1).getState());
        assertEquals(State.DELETING, instanceService.refresh(instance2).getState());
    }

    private Title createTitle() {
        var titleForm = titleService.newTitleForm(Collections.emptySet(), Collections.emptySet());
        titleForm.setSeedUrls("http://example.org/");
        titleForm.setName("Test Title");
        return titleService.save(titleForm, userService.getCurrentUser());
    }
}
