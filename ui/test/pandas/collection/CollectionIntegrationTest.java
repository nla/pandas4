package pandas.collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import pandas.IntegrationTest;
import pandas.core.PandasUserDetailsService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    @Transactional
    @WithUserDetails("admin")
    void showsOnlyNominationActionToInfoUsers() throws Exception {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                authentication.getPrincipal(), authentication.getCredentials(),
                PandasUserDetailsService.authoritiesForRoleType("infouser")));

        Collection collection = new Collection();
        collection.setName("Info user collection");
        collection = collectionRepository.save(collection);

        mockMvc.perform(get("/collections/{id}", collection.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nominate a website")))
                .andExpect(content().string(not(containsString("Add a website"))))
                .andExpect(content().string(not(containsString("Bulk add websites"))));
    }

    @Test
    @Transactional
    @WithUserDetails("admin")
    void usersWithoutEditCollectionsPrivilegeCannotCreateOrEditCollections() throws Exception {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                authentication.getPrincipal(), authentication.getCredentials(),
                List.of(new SimpleGrantedAuthority("ROLE_infouser"))));

        Collection collection = new Collection();
        collection.setName("Protected collection");
        collection = collectionRepository.save(collection);

        mockMvc.perform(get("/collections"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("href=\"/collections/new\""))));
        mockMvc.perform(get("/collections/{id}", collection.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("href=\"/collections/" + collection.getId() + "/edit\""))))
                .andExpect(content().string(not(containsString("href=\"/collections/new?parent="))));
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("href=\"/collections/new\""))));

        mockMvc.perform(get("/collections/new"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/collections/new").with(csrf())
                        .param("name", "Unauthorized collection")
                        .param("displayed", "false")
                        .param("closed", "false"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/collections/{id}/edit", collection.getId()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/collections/{id}/edit", collection.getId()).with(csrf())
                        .param("name", "Unauthorized edit")
                        .param("displayed", "false")
                        .param("closed", "false"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/collections/{id}/delete", collection.getId()).with(csrf()))
                .andExpect(status().isForbidden());
    }
}
