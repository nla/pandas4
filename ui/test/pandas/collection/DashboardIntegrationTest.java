package pandas.collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import pandas.IntegrationTest;
import pandas.agency.User;
import pandas.agency.UserRepository;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardIntegrationTest extends IntegrationTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TitleRepository titleRepository;

    @Test
    @Transactional
    @WithUserDetails("admin")
    void rendersRecentTitle() throws Exception {
        User admin = userRepository.findByUserid("admin").orElseThrow();
        Instant now = Instant.now();
        Title title = new Title(admin, now);
        title.setName("Recent test title");
        title.changeStatus(Status.NOMINATED, null, admin, now);
        titleRepository.save(title);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Recent test title")));
    }
}
