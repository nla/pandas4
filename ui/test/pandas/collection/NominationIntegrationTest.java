package pandas.collection;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import pandas.IntegrationTest;
import pandas.agency.User;
import pandas.agency.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@WithUserDetails("admin")
class NominationIntegrationTest extends IntegrationTest {
    @Autowired
    private CollectionRepository collectionRepository;
    @Autowired
    private TitleRepository titleRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;
    @MockitoBean
    private TitleSearcher titleSearcher;
    @MockitoBean
    private CaptureIndex captureIndex;

    @Test
    void rendersFixedCollectionForm() throws Exception {
        Collection collection = collection("Nomination form collection", false);

        mockMvc.perform(get("/nominate").param("collection", collection.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nominate a website")))
                .andExpect(content().string(containsString("Nomination form collection")))
                .andExpect(content().string(containsString("See your previous nominations")))
                .andExpect(content().string(containsString("nominator=")))
                .andExpect(content().string(not(containsString("Choose a collection"))))
                .andExpect(content().string(not(containsString("Website name"))));
    }

    @Test
    void createsCreatorOwnedNominatedTitleWithHistory() throws Exception {
        Collection firstCollection = collection("First nomination target", false);
        Collection secondCollection = collection("Second nomination target", false);

        mockMvc.perform(post("/nominate")
                        .with(csrf())
                        .param("collection", firstCollection.getId().toString(), secondCollection.getId().toString())
                        .param("collectionId", secondCollection.getId().toString())
                        .param("seedUrl", "example.net/path")
                        .param("context", "Useful context for the reviewer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/titles/*"));

        Title title = titleRepository.findByTitleUrlIn(List.of("http://example.net/path")).get(0);
        User nominator = userRepository.findByUserid("admin").orElseThrow();
        assertEquals("example.net", title.getName());
        assertEquals("Useful context for the reviewer", title.getNotes());
        assertEquals(Status.NOMINATED, title.getStatus());
        assertEquals(nominator, title.getOwner());
        assertEquals(nominator, title.getNominator());
        assertEquals(nominator.getAgency(), title.getAgency());
        assertEquals(Set.of(secondCollection), title.getCollections());
        assertFalse(title.getOwnerHistories().isEmpty());
        assertEquals(nominator, title.getOwnerHistories().get(0).getUser());
        assertFalse(title.getStatusHistories().isEmpty());
        assertEquals(Status.NOMINATED, title.getStatusHistories().get(0).getStatus());
        assertEquals(nominator, title.getStatusHistories().get(0).getUser());
    }

    @Test
    void nominatedWorktrayShowsTriageInformation() throws Exception {
        User nominator = userRepository.findByUserid("admin").orElseThrow();
        Collection collection = collection("Triage collection", false);
        Instant registered = Instant.now().minus(3, ChronoUnit.DAYS);

        Title withContext = new Title(nominator, registered);
        withContext.setName("Nomination with context");
        withContext.setTitleUrl("https://context.example.org");
        withContext.setNotes("Time-critical collecting context for the reviewer");
        withContext.setCollections(Set.of(collection));
        withContext.changeStatus(Status.NOMINATED, null, nominator, registered);
        titleRepository.save(withContext);
        entityManager.flush();
        withContext.setRegDate(registered);
        titleRepository.save(withContext);

        Title withoutContext = new Title(nominator, registered);
        withoutContext.setName("Nomination without context");
        withoutContext.setTitleUrl("https://no-context.example.org");
        withoutContext.setCollections(Set.of(collection));
        withoutContext.changeStatus(Status.NOMINATED, null, nominator, registered);
        titleRepository.save(withoutContext);
        entityManager.flush();
        withoutContext.setRegDate(registered);
        titleRepository.save(withoutContext);
        entityManager.flush();

        String agencyAlias = nominator.getAgency().getOrganisation().getAlias();
        mockMvc.perform(get("/worktrays/" + agencyAlias + "/nominated"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Triage collection")))
                .andExpect(content().string(containsString("Time-critical collecting context for the reviewer")))
                .andExpect(content().string(not(containsString("No context provided"))))
                .andExpect(content().string(not(containsString("Collections:"))))
                .andExpect(content().string(containsString("admin</a> nominated </span><time")))
                .andExpect(content().string(not(containsString("Nominated title</th>"))))
                .andExpect(content().string(not(containsString("owned by"))))
                .andExpect(content().string(containsString("title=\"Registered ")))
                .andExpect(content().string(containsString("3 days ago")));
    }

    @Test
    void copiesSelectedCollectionSubjectsToNominatedTitle() throws Exception {
        Subject subject = new Subject();
        subject.setName("Nomination subject");
        subject = subjectRepository.save(subject);
        Collection root = collection("Subject nomination root", false);
        Collection selectedCollection = childCollection(root, "Subject nomination target", false);
        selectedCollection.getSubjects().add(subject);
        collectionRepository.save(selectedCollection);

        mockMvc.perform(post("/nominate")
                        .with(csrf())
                        .param("collection", root.getId().toString())
                        .param("collectionId", selectedCollection.getId().toString())
                        .param("seedUrl", "https://subject.example.org"))
                .andExpect(status().is3xxRedirection());

        Title title = titleRepository.findByTitleUrlIn(List.of("https://subject.example.org")).get(0);
        assertEquals(Set.of(subject), title.getSubjects());
    }

    @Test
    void allowsSelectingARootOrDescendantCollection() throws Exception {
        Collection firstRoot = collection("First selectable root", false);
        Collection child = childCollection(firstRoot, "Child destination", false);
        Collection grandchild = childCollection(child, "Grandchild destination", false);
        Collection secondRoot = collection("Second selectable root", false);

        mockMvc.perform(get("/nominate")
                        .param("collection", firstRoot.getId().toString(), secondRoot.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<label for=collectionId>Collection</label>")))
                .andExpect(content().string(containsString("<option value=\"\"></option>")))
                .andExpect(content().string(containsString(option(firstRoot, firstRoot.getFullName()))))
                .andExpect(content().string(containsString(option(child, child.getFullName()))))
                .andExpect(content().string(containsString(option(grandchild, grandchild.getFullName()))))
                .andExpect(content().string(containsString(option(secondRoot, secondRoot.getFullName()))));

        mockMvc.perform(post("/nominate")
                        .with(csrf())
                        .param("collection", firstRoot.getId().toString(), secondRoot.getId().toString())
                        .param("collectionId", grandchild.getId().toString())
                        .param("seedUrl", "https://descendant.example.org"))
                .andExpect(status().is3xxRedirection());

        Title title = titleRepository.findByTitleUrlIn(List.of("https://descendant.example.org")).get(0);
        assertEquals(Set.of(grandchild), title.getCollections());
    }

    @Test
    void showsCollectionDropdownForASingleRootWithChildren() throws Exception {
        Collection root = collection("Parent nomination target", false);
        Collection child = childCollection(root, "Specific nomination target", false);
        Collection grandchild = childCollection(child, "Narrow nomination target", false);

        mockMvc.perform(get("/nominate").param("collection", root.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<label for=collectionId>Subcollection</label>")))
                .andExpect(content().string(containsString("<option value=\"\"></option>")))
                .andExpect(content().string(not(containsString(option(root, root.getFullName())))))
                .andExpect(content().string(containsString(option(child, "Specific nomination target"))))
                .andExpect(content().string(containsString(option(grandchild,
                        "Specific nomination target—Narrow nomination target"))));
    }

    @Test
    void requiresAndValidatesSelectedCollection() throws Exception {
        Collection allowedRoot = collection("Allowed nomination root", false);
        childCollection(allowedRoot, "Allowed nomination child", false);
        Collection unrelated = collection("Unrelated nomination target", false);

        mockMvc.perform(post("/nominate")
                        .with(csrf())
                        .param("collection", allowedRoot.getId().toString())
                        .param("seedUrl", "https://missing-selection.example.org"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Select a collection")));

        mockMvc.perform(post("/nominate")
                        .with(csrf())
                        .param("collection", allowedRoot.getId().toString())
                        .param("collectionId", allowedRoot.getId().toString())
                        .param("seedUrl", "https://parent-selection.example.org"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/nominate")
                        .with(csrf())
                        .param("collection", allowedRoot.getId().toString())
                        .param("collectionId", unrelated.getId().toString())
                        .param("seedUrl", "https://invalid-selection.example.org"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usesHostnameWhenTitleIsBlank() throws Exception {
        Collection collection = collection("Fallback title target", false);

        mockMvc.perform(post("/nominate")
                        .with(csrf())
                        .param("collection", collection.getId().toString())
                        .param("seedUrl", "https://sub.example.org/page")
                        .param("name", ""))
                .andExpect(status().is3xxRedirection());

        Title title = titleRepository.findByTitleUrlIn(List.of("https://sub.example.org/page")).get(0);
        assertEquals("sub.example.org", title.getName());
    }

    @Test
    void duplicateCheckIncludesTheLatestArchivedCopy() throws Exception {
        when(titleSearcher.urlCheck(anyString())).thenReturn(List.of(
                new TitleSearcher.UrlCheckResult(1L, 2L, "Existing title", "https://example.org/", null)));
        Capture capture = new Capture("org,example)/ 20260916010203 https://example.org/ text/html 200 " +
                "sha1:ABC - - 100 0 example.warc.gz");
        when(captureIndex.latestSuccessful(anyString())).thenReturn(Optional.of(capture));

        mockMvc.perform(get("/nominate/check").param("url", "https://example.org/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existingTitle").value(true))
                .andExpect(jsonPath("$.latestSnapshot.date").value("2026-09-16T01:02:03Z"))
                .andExpect(jsonPath("$.latestSnapshot.url")
                        .value("https://webarchive.nla.gov.au/awa/20260916010203/https://example.org/"));
    }

    @Test
    void returnsValidationErrorForInvalidUrl() throws Exception {
        Collection collection = collection("Validation target", false);

        mockMvc.perform(post("/nominate")
                        .with(csrf())
                        .param("collection", collection.getId().toString())
                        .param("seedUrl", "not a URL"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Enter a valid HTTP or HTTPS URL")));
    }

    @Test
    void rejectsClosedCollectionOnGetAndPost() throws Exception {
        Collection collection = collection("Closed nomination target", true);
        Collection openCollection = collection("Open nomination target", false);

        mockMvc.perform(get("/nominate").param("collection", collection.getId().toString()))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/nominate")
                        .with(csrf())
                        .param("collection", openCollection.getId().toString(), collection.getId().toString())
                        .param("seedUrl", "https://example.com"))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsClosedAncestor() throws Exception {
        Collection parent = collection("Closed parent", true);
        Collection child = collection("Open child", false);
        child.setParent(parent);
        child = collectionRepository.save(child);

        mockMvc.perform(get("/nominate").param("collection", child.getId().toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void validatesLinkParametersAndUnknownCollection() throws Exception {
        mockMvc.perform(get("/nominate"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/nominate").param("collection", "999999999"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/nominate").param("parent", "1"))
                .andExpect(status().isBadRequest());
    }

    private Collection collection(String name, boolean closed) {
        Collection collection = new Collection();
        collection.setName(name);
        collection.setClosed(closed);
        return collectionRepository.save(collection);
    }

    private Collection childCollection(Collection parent, String name, boolean closed) {
        Collection child = new Collection();
        child.setName(name);
        child.setClosed(closed);
        child.setParent(parent);
        return collectionRepository.save(child);
    }

    private String option(Collection collection, String label) {
        return "<option value=\"" + collection.getId() + "\">" + label + "</option>";
    }
}
