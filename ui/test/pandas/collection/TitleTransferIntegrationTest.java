package pandas.collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import pandas.IntegrationTest;
import pandas.agency.User;
import pandas.agency.UserRepository;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TitleTransferIntegrationTest extends IntegrationTest {
    @Autowired
    TitleService titleService;
    @Autowired
    UserRepository userRepository;

    @Test
    @Transactional
    @WithUserDetails("admin")
    void transferNoteIsShownToRecipient() throws Exception {
        User admin = userRepository.findByUserid("admin").orElseThrow();
        User previousOwner = new User(admin.getAgency());
        previousOwner.setUserid("transfer-sender");
        previousOwner.setNameGiven("Sally");
        previousOwner.setNameFamily("Sender");
        userRepository.save(previousOwner);

        var titleForm = titleService.newTitleForm(Collections.emptySet(), Collections.emptySet());
        titleForm.setSeedUrls("http://example.org/transfer-test/");
        titleForm.setName("Transfer Note Test Title");
        Title title = titleService.save(titleForm, previousOwner);

        titleService.transferOwnership(title, admin.getAgency(), admin,
                "Please look after this one.\nIt needs a new profile.", previousOwner);

        mockMvc.perform(get("/titles/" + title.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sally Sender")))
                .andExpect(content().string(containsString("Please look after this one.\nIt needs a new profile.")));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Transfer Note Test Title")))
                .andExpect(content().string(containsString("(from transfer-sender)")))
                .andExpect(content().string(containsString("Please look after this one.\nIt needs a new profile.")));
    }
}
