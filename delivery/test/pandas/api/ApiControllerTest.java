package pandas.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import pandas.collection.*;
import pandas.gather.GatherMethodRepository;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url = jdbc:h2:mem:it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.properties.hibernate.dialect = pandas.core.PandasH2Dialect"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TitleService titleService;
    @Autowired
    private CollectionRepository collectionRepository;

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

        TitleEditForm form = titleService.newTitleForm(Set.of(), List.of());
        form.setName("Test title");
        form.setGatherMethod(gatherMethodRepository.findByName("Heritrix").orElseThrow());
        form.setTitleUrl("http://example.com/");
        form.getCollections().add(parentCollection);
        title = titleService.save(form, null);
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
                .andExpect(jsonPath("$.subcollections[0].name").value(childCollection.getName()));
    }

}