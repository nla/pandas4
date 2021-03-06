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
import pandas.collection.*;
import pandas.gather.*;
import pandas.gatherer.heritrix.HeritrixClient;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Integration tests for PANDAS Gatherer
 */
@SpringBootTest(classes = PandasGatherer.class, properties = {
        "spring.datasource.url = jdbc:h2:mem:it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "heritrix.url = https://localhost:18443/engine",
        "heritrix.password = password",
        "bamboo.crawlSeriesId = 1"})
@ContextConfiguration(initializers = GathererIT.Initializer.class)
@MockServerTest("bamboo.url = http://127.0.0.1:${mockServerPort}/bamboo")
public class GathererIT {
    @TempDir static Path tempDir;
    @Autowired TitleService titleService;
    @Autowired GatherMethodRepository gatherMethodRepository;
    @Autowired
    TitleRepository titleRepository;
    @Autowired
    TepRepository tepRepository;
    @Autowired
    InstanceRepository instanceRepository;
    @Autowired
    InstanceService instanceService;

    MockServerClient mockServer;

    @Test
    @Timeout(30)
    public void testHeritrixCrawl() throws IOException, InterruptedException {
        mockServer.when(request().withPath("/bamboo/instances/1")).respond(response().withStatusCode(404));
        mockServer.when(request().withMethod("POST").withPath("/bamboo/crawls/new")).respond(response().withHeader("Location", "/bamboo/crawls/1"));
        mockServer.when(request().withMethod("PUT").withPath("/bamboo/crawls/1/artifacts/.*")).respond(response());
        mockServer.when(request().withMethod("PUT").withPath("/bamboo/crawls/1/warcs/.*")).respond(response());

        int heritrixPort = 18443;

        String javaExe = Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java").toString();
        Path heritrixWorking = Files.createDirectories(tempDir.resolve("heritrix"));
        Path heritrixStdio = heritrixWorking.resolve("stdio");
        Process process = new ProcessBuilder(javaExe, "-cp", Paths.get("target/dependency/heritrix-3.4.0-20200518/lib/*").toAbsolutePath().toString(),
                "-Xmx512m",
                "org.archive.crawler.Heritrix", "-a", "password", "-p", Integer.toString(heritrixPort))
                .directory(heritrixWorking.toFile())
                .redirectOutput(heritrixStdio.toFile())
                .redirectErrorStream(true)
                .start();

        try {
            // wait for Heritrix to start
            HeritrixClient heritrixClient = new HeritrixClient("https://127.0.0.1:" + heritrixPort + "/engine", "admin", "password");
            for (int i = 0; i < 100; i++) {
                try {
                    heritrixClient.getEngine();
                    System.err.println("Heritrix started");
                    break;
                } catch (ConnectException e) {
                    System.err.println("Waiting for Heritrix...");
                    Thread.sleep(100);
                }
            }

            TitleEditForm form = titleService.newTitleForm(List.of(), List.of());
            form.setName("Heritrix title");
            form.setGatherMethod(gatherMethodRepository.findByName("Heritrix").orElseThrow());
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
        } finally {
            System.err.println("Killing Heritrix");
            process.destroyForcibly();
            if (Files.exists(heritrixStdio)) {
                System.err.println("Heritrix stdio: \n" + Files.readString(heritrixStdio));
            }
        }
    }

    @Test
    @Timeout(value = 10)
    public void testHttrackCrawl() throws InterruptedException {
        mockServer.when(request().withMethod("GET").withPath("/target/")).respond(response().withBody("test page"));
        mockServer.when(request().withMethod("POST").withPath("/bamboo/crawls/new")).respond(response().withHeader("Location", "/bamboo/crawls/1"));
        mockServer.when(request().withMethod("PUT").withPath("/bamboo/crawls/1/artifacts/.*")).respond(response());
        mockServer.when(request().withMethod("PUT").withPath("/bamboo/crawls/1/warcs/.*")).respond(response());

        TitleEditForm form = titleService.newTitleForm(List.of(), List.of());
        form.setName("HTTrack title");
        form.setGatherMethod(gatherMethodRepository.findByName("HTTrack").orElseThrow());
        form.setTitleUrl("http://127.0.0.1:" + mockServer.getPort() + "/target/");
        form.setOneoffDates(List.of(Instant.now()));
        Title title = titleService.save(form, null);

        Tep tep = new Tep();
        tep.setTitle(title);
        title.setTep(tep);
        titleRepository.save(title);

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
