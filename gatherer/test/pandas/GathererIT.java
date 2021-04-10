package pandas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.mockserver.client.MockServerClient;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import pandas.collection.Title;
import pandas.collection.TitleEditForm;
import pandas.collection.TitleRepository;
import pandas.collection.TitleService;
import pandas.gather.*;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Integration tests for PANDAS Gatherer
 */
@SpringBootTest(classes = PandasGatherer.class, properties = { "spring.datasource.url = jdbc:h2:mem:it;DB_CLOSE_DELAY=-1" })
@ContextConfiguration(initializers = GathererIT.Initializer.class)
@MockServerTest
public class GathererIT {
    @TempDir static Path tempDir;
    @Autowired TitleService titleService;
    @Autowired GatherMethodRepository gatherMethodRepository;
    @Autowired
    TitleRepository titleRepository;
    @Autowired
    InstanceRepository instanceRepository;
    @Autowired
    InstanceService instanceService;

    MockServerClient mockServer;

    @Test
    @Timeout(value = 10)
    public void testHttrackCrawl() throws InterruptedException {
        mockServer.when(request().withMethod("GET").withPath("/target/")).respond(response().withBody("test page"));

        TitleEditForm form = titleService.newTitleForm(List.of(), List.of());
        form.setName("Test title");
        form.setGatherMethod(gatherMethodRepository.findByName("HTTrack").orElseThrow());
        form.setTitleUrl("http://127.0.0.1:" + mockServer.getPort() + "/target/");
        form.setOneoffDates(List.of(Instant.now()));
        Title title = titleService.save(form, null);

        // wait for instance to be created
        List<Instance> instances = instanceRepository.findByTitle(title);
        while (instances.isEmpty()) {
            Thread.sleep(100);
            instances = instanceRepository.findByTitle(title);
        }
        Instance instance = instances.get(0);

        // wait until gathering finishes
        while (Set.of(State.GATHERING, State.CREATION, State.GATHER_PROCESS).contains(instance.getState().getName())) {
            Thread.sleep(100);
            instance = instanceRepository.findById(instance.getId()).orElseThrow();
        }
        assertEquals(State.GATHERED, instance.getState().getName());

        // archive the instance
        instanceService.updateState(instance, State.ARCHIVING);

        // wait until archiving finishes
        while (instance.getState().getName().equals(State.ARCHIVING)) {
            Thread.sleep(100);
            instance = instanceRepository.findById(instance.getId()).orElseThrow();
        }
        assertEquals(State.ARCHIVED, instance.getState().getName());
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of("PANDAS_HOME=" + tempDir).applyTo(context);
        }
    }
}
