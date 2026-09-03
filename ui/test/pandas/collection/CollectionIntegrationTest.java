package pandas.collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import pandas.IntegrationTest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CollectionIntegrationTest extends IntegrationTest {
    @Autowired
    private CollectionRepository collectionRepository;

    @Test
    @Transactional
    @WithUserDetails("admin")
    void sanitizesDescriptionWhenRenderingCollection() throws Exception {
        Collection collection = new Collection();
        collection.setName("Sanitizer test collection");
        collection.setDescription("<script>alert('unsafe')</script><strong>Safe description</strong>");
        collection = collectionRepository.save(collection);

        mockMvc.perform(get("/collections/{id}", collection.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<strong>Safe description</strong>")))
                .andExpect(content().string(not(containsString("<script>"))));
    }
}
