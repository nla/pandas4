package pandas.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import pandas.collection.*;
import pandas.core.TempDataPathInitializer;
import pandas.gather.*;

import java.time.Instant;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url = jdbc:h2:mem:it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "CDX_URL = http://dummy.cdx.server.example"
})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = TempDataPathInitializer.class)
class ApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TitleService titleService;
    @Autowired
    private CollectionRepository collectionRepository;
    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private GatherMethodRepository gatherMethodRepository;
    private Title title;
    private Collection parentCollection;
    private Collection childCollection;

    @BeforeAll
    public void setUp() {
        parentCollection = new Collection();
        parentCollection.setName("Parent collection");
        parentCollection = collectionRepository.save(parentCollection);

        childCollection = new Collection();
        childCollection.setName("Child collection");
        childCollection.setParent(parentCollection);
        childCollection = collectionRepository.save(childCollection);

        TitleEditForm form = titleService.newTitleForm(Set.of(), Set.of());
        form.setName("Test title");
        form.setGatherMethod(gatherMethodRepository.findByName("Heritrix").orElseThrow());
        form.setSeedUrls("http://example.com/");
        form.getCollections().add(parentCollection);
        title = titleService.save(form, null);

        var archivedState = stateRepository.findByName(State.ARCHIVED).orElseThrow();

        Instance hiddenInstance = new Instance();
        hiddenInstance.setTitle(title);
        hiddenInstance.setDate(Instant.parse("2022-12-19T01:02:03Z"));
        hiddenInstance.setIsDisplayed(false);
        hiddenInstance.setState(archivedState);

        Instance visibleInstance = new Instance();
        visibleInstance.setTitle(title);
        visibleInstance.setDate(Instant.parse("2022-12-20T04:56:55Z"));
        visibleInstance.setIsDisplayed(true);
        visibleInstance.setState(archivedState);

        instanceRepository.save(visibleInstance);
    }

    @Test
    public void test() throws Exception {
        mockMvc.perform(get("/api")).andExpect(status().isOk())
                .andExpect(content().string(containsString("PANDORA API")));
    }

    @Test
    public void tep() throws Exception {
        mockMvc.perform(get("/api/tep/" + title.getPi())).andExpect(status().isOk())//.andDo(print())
                .andExpect(jsonPath("$.url").value(title.getTitleUrl()))
                .andExpect(jsonPath("$.name").value(title.getName()))
                .andExpect(jsonPath("$.collections[0].id").value(parentCollection.getId()))
                .andExpect(jsonPath("$.collections[0].name").value(parentCollection.getName()));
    }

    @Test
    public void collection() throws Exception {
        mockMvc.perform(get("/api/collection/" + parentCollection.getId())).andExpect(status().isOk())//.andDo(print())
                .andExpect(jsonPath("$.id").value(parentCollection.getId()))
                .andExpect(jsonPath("$.name").value(parentCollection.getName()))
                .andExpect(jsonPath("$.snapshots[0].date").value("2022-12-20T04:56:55.000+0000"))
                .andExpect(jsonPath("$.subcollections[0].name").value(childCollection.getName()));
    }
}