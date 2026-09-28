package pandas.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import pandas.IntegrationTest;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.agency.User;
import pandas.agency.UserRepository;
import pandas.core.Organisation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Not transactional because the nominated worktray is served from the search index, which is only updated when
 * transactions commit. Each test uses its own agency so the committed titles don't affect other tests.
 */
@WithUserDetails("admin")
class NominatedWorktrayIntegrationTest extends IntegrationTest {
    @Autowired
    private AgencyRepository agencyRepository;
    @Autowired
    private CollectionRepository collectionRepository;
    @Autowired
    private TitleRepository titleRepository;
    @Autowired
    private UserRepository userRepository;

    private String alias;
    private Agency agency;

    @BeforeEach
    void createAgency() {
        alias = "nomtest-" + UUID.randomUUID();
        Organisation organisation = new Organisation();
        organisation.setName("Nominated worktray test agency");
        organisation.setAlias(alias);
        agency = new Agency();
        agency.setOrganisation(organisation);
        agency = agencyRepository.save(agency);
    }

    @Test
    void showsTriageInformation() throws Exception {
        User alice = user("Alice");
        Collection collection = collection("Triage collection");
        Instant registered = Instant.now().minus(3, ChronoUnit.DAYS);

        Title withContext = nominate("Nomination with context", alice, registered, collection);
        withContext.setNotes("Time-critical collecting context for the reviewer");
        titleRepository.save(withContext);
        nominate("Nomination without context", alice, registered, collection);

        mockMvc.perform(get("/worktrays/" + alias + "/nominated"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Triage collection")))
                .andExpect(content().string(containsString("Time-critical collecting context for the reviewer")))
                .andExpect(content().string(not(containsString("No context provided"))))
                .andExpect(content().string(not(containsString("Collections:"))))
                .andExpect(content().string(containsString(alice.getUserid() + "</a> nominated </span><time")))
                .andExpect(content().string(not(containsString("Nominated title</th>"))))
                .andExpect(content().string(not(containsString("owned by"))))
                .andExpect(content().string(containsString("title=\"Registered ")))
                .andExpect(content().string(containsString("3 days ago")));
    }

    @Test
    void filtersByCollectionAndNominatorAndSorts() throws Exception {
        User alice = user("Alice");
        User bob = user("Bob");
        Collection shared = collection("Worktray shared");
        Collection sport = collection("Worktray sport");
        Collection music = collection("Worktray music");
        Instant now = Instant.now();
        nominate("Filter title apple", alice, now.minus(3, ChronoUnit.DAYS), shared, sport);
        nominate("Filter title banana", bob, now.minus(2, ChronoUnit.DAYS), shared, music);
        Title awaiting = nominate("Filter title cherry", alice, now.minus(1, ChronoUnit.DAYS), music);
        awaiting.setAwaitingConfirmation(true);
        titleRepository.save(awaiting);

        String url = "/worktrays/" + alias + "/nominated";
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nominated Titles (<span>2</span>)")))
                .andExpect(content().string(stringContainsInOrder("Filter title banana", "Filter title apple")))
                .andExpect(content().string(not(containsString("Filter title cherry"))))
                .andExpect(content().string(containsString("Worktray sport")))
                .andExpect(content().string(containsString("Bob Tester")));

        mockMvc.perform(get(url).param("sort", "Oldest"))
                .andExpect(status().isOk())
                .andExpect(content().string(stringContainsInOrder("Filter title apple", "Filter title banana")));

        mockMvc.perform(get(url).param("sort", "Name (ascending)"))
                .andExpect(status().isOk())
                .andExpect(content().string(stringContainsInOrder("Filter title apple", "Filter title banana")));

        mockMvc.perform(get(url).param("collection", sport.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Filter title apple")))
                .andExpect(content().string(not(containsString("Filter title banana"))));

        mockMvc.perform(get(url).param("nominator", bob.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Filter title banana")))
                .andExpect(content().string(not(containsString("Filter title apple"))));

        // pagination links keep the filters
        mockMvc.perform(get(url).param("collection", shared.getId().toString()).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "href=\"?collection=" + shared.getId() + "&amp;size=1&amp;page=1\"")));
    }

    private User user(String givenName) {
        User user = new User(agency);
        user.setUserid(givenName.toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8));
        user.setNameGiven(givenName);
        user.setNameFamily("Tester");
        return userRepository.save(user);
    }

    private Collection collection(String name) {
        Collection collection = new Collection();
        collection.setName(name);
        return collectionRepository.save(collection);
    }

    private Title nominate(String name, User nominator, Instant registered, Collection... collections) {
        Title title = new Title(nominator, registered);
        title.setName(name);
        title.setTitleUrl("https://" + name.replace(' ', '-').toLowerCase() + ".example.org/");
        title.setCollections(Set.of(collections));
        title.changeStatus(Status.NOMINATED, null, nominator, registered);
        title = titleRepository.save(title);
        // registration date is overwritten by auditing on first save
        title.setRegDate(registered);
        return titleRepository.save(title);
    }
}
