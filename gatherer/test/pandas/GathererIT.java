package pandas;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pandas.collection.Title;
import pandas.collection.TitleEditForm;
import pandas.collection.TitleRepository;
import pandas.collection.TitleService;
import pandas.gather.*;
import pandas.gatherer.heritrix.HeritrixClient;
import pandas.gatherer.heritrix.HeritrixProcess;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static pandas.gather.InstanceThumbnail.Type.LIVE;
import static pandas.gather.InstanceThumbnail.Type.REPLAY;

/**
 * Integration tests for PANDAS Gatherer
 */
@SpringBootTest(classes = PandasGatherer.class, properties = {
        "spring.datasource.url = jdbc:h2:mem:it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "heritrix.url = https://localhost:18443/engine",
        "heritrix.password = password",
        "bamboo.crawlSeriesId = 1",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
@ContextConfiguration(initializers = GathererIT.Initializer.class)
public class GathererIT {
    @TempDir static Path tempDir;
    @Autowired TitleService titleService;
    @Autowired GatherMethodRepository gatherMethodRepository;
    @Autowired
    TitleRepository titleRepository;
    @Autowired
    InstanceRepository instanceRepository;
    @Autowired
    InstanceThumbnailRepository instanceThumbnailRepository;
    @Autowired
    InstanceService instanceService;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private static HttpServer server;

    @BeforeAll
    public static void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/target/", exchange -> {
            if (exchange.getRequestURI().getPath().equals("/target/redir")) {
                exchange.getResponseHeaders().add("Location", "/target/dest");
                exchange.sendResponseHeaders(302, -1);
            } else if (exchange.getRequestURI().getPath().equals("/target/dest")) {
                var response = "dest page";
                exchange.sendResponseHeaders(200, response.length());
                try (var os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                var response = "test page";
                exchange.sendResponseHeaders(200, response.length());
                try (var os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });
        server.createContext("/bamboo/instances/1", exchange -> {
            exchange.sendResponseHeaders(404, -1);
        });
        server.createContext("/bamboo/crawls/new", exchange -> {
            if (exchange.getRequestMethod().equals("POST")) {
                exchange.getResponseHeaders().add("Location", "/bamboo/crawls/1");
                exchange.sendResponseHeaders(200, -1);
            }
        });
        server.createContext("/bamboo/crawls/1/artifacts/", exchange -> {
            if (exchange.getRequestMethod().equals("PUT")) {
                exchange.sendResponseHeaders(200, -1);
            }
        });
        server.createContext("/bamboo/crawls/1/warcs/", exchange -> {
            if (exchange.getRequestMethod().equals("PUT")) {
                exchange.sendResponseHeaders(200, -1);
            }
        });
        server.start();
    }

    @AfterAll
    public static void tearDown() {
        if (server != null) server.stop(0);
    }

    @DynamicPropertySource
    static void registerBambooUrl(DynamicPropertyRegistry registry) {
        int port = server.getAddress().getPort();
        String url = "http://127.0.0.1:" + port + "/bamboo";
        registry.add("bamboo.url", () -> url);
    }

    @Test
    @Timeout(30)
    public void testHeritrixCrawl() throws IOException, InterruptedException {
        int serverPort = server.getAddress().getPort();
        int heritrixPort = 18443;
        Path heritrixWorking = Files.createDirectories(tempDir.resolve("heritrix"));
        String password = "password";
        Path heritrixStdio = null;
        Path heritrixHome = Paths.get("target/dependency/heritrix-3.5.0");
        assumeTrue(Files.exists(heritrixHome), "Heritrix not found in " + heritrixHome);
        try (HeritrixProcess heritrixProcess = new HeritrixProcess(heritrixHome, heritrixWorking, heritrixPort, password)) {
            heritrixStdio = heritrixProcess.getStdio();
            // wait for Heritrix to start
            HeritrixClient heritrixClient = heritrixProcess.getClient();
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

            TitleEditForm form = titleService.newTitleForm(Set.of(), Set.of());
            form.setName("Heritrix title");
            form.setGatherMethod(gatherMethodRepository.findByName("Heritrix").orElseThrow());
            String seedUrl = "http://127.0.0.1:" + serverPort + "/target/";
            String redirSeedUrl = "http://127.0.0.1:" + serverPort + "/target/redir";
            String redirDestUrl = "http://127.0.0.1:" + serverPort + "/target/dest";
            form.setSeedUrls(seedUrl + "\n" + redirSeedUrl);
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
            Long instanceId = instance.getId();
            while (Set.of(State.GATHERING, State.CREATION, State.GATHER_PROCESS).contains(instance.getState())) {
                Thread.sleep(100);
                instance = instanceRepository.getOrThrow(instanceId);
            }

            assertEquals(State.GATHERED, instance.getState());

            // archive the instance
            instanceService.updateState(instance.getId(), State.ARCHIVING);

            // wait until archiving finishes
            while (instance.getState().equals(State.ARCHIVING)) {
                Thread.sleep(100);
                instance = instanceRepository.getOrThrow(instanceId);
            }
            assertEquals(State.ARCHIVED, instance.getState());

            assertNotNull(instance.getGather().getFiles(), "missing files gather stat");
            assertNotNull(instance.getGather().getSize(), "missing size gather stat");

            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.execute(status -> {
                var instance2 = instanceRepository.getOrThrow(instanceId);
                assertEquals(200, instance2.getSeed(seedUrl).getStatus());
                assertEquals(302, instance2.getSeed(redirSeedUrl).getStatus());
                assertEquals(redirDestUrl, instance2.getSeed(redirSeedUrl).getRedirect());
                assertEquals(200, instance2.getSeed(redirDestUrl).getStatus());
                return null;
            });

            assertTrue(instanceThumbnailRepository.existsByInstanceAndType(instance, LIVE), "missing live thumbnail");
            assertTrue(instanceThumbnailRepository.existsByInstanceAndType(instance, REPLAY), "missing replay thumbnail");
        } finally {
            if (heritrixStdio != null && Files.exists(heritrixStdio)) {
                System.err.println("Heritrix stdio: \n" + Files.readString(heritrixStdio));
            }
        }
    }

    @Test
    @Timeout(value = 10)
    public void testHttrackCrawl() throws InterruptedException {
        int serverPort = server.getAddress().getPort();
        TitleEditForm form = titleService.newTitleForm(Set.of(), Set.of());
        form.setName("HTTrack title");
        form.setGatherMethod(gatherMethodRepository.findByName("HTTrack").orElseThrow());
        form.setSeedUrls("http://127.0.0.1:" + serverPort + "/target/");
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
        while (Set.of(State.GATHERING, State.CREATION, State.GATHER_PROCESS).contains(instance.getState())) {
            Thread.sleep(100);
            instance = instanceRepository.findById(instance.getId()).orElseThrow();
        }

        assertEquals(State.GATHERED, instance.getState());

        // archive the instance
        instanceService.updateState(instance.getId(), State.ARCHIVING);

        // wait until archiving finishes
        while (instance.getState().equals(State.ARCHIVING)) {
            Thread.sleep(100);
            instance = instanceRepository.findById(instance.getId()).orElseThrow();
        }
        assertEquals(State.ARCHIVED, instance.getState());
        assertTrue(instanceThumbnailRepository.existsByInstanceAndType(instance, LIVE));
    }

    @Test
    @Timeout(value = 120)
    public void testBrowsertrixCrawl() throws InterruptedException, IOException {
        try {
            Process process = new ProcessBuilder("podman", "version").inheritIO().start();
            Assumptions.assumeTrue(process.waitFor() == 0, "'podman version' exited with non-zero status");
        } catch (IOException e) {
            Assumptions.assumeTrue(false, "Error starting podman");
        }

        int serverPort = server.getAddress().getPort();

        TitleEditForm form = titleService.newTitleForm(Set.of(), Set.of());
        form.setName("Browsertrix title");
        form.setGatherMethod(gatherMethodRepository.findByName(GatherMethod.BROWSERTRIX).orElseThrow());
        form.setSeedUrls("http://127.0.0.1:" + serverPort + "/target/");
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
        while (Set.of(State.GATHERING, State.CREATION, State.GATHER_PROCESS).contains(instance.getState())) {
            Thread.sleep(100);
            instance = instanceRepository.findById(instance.getId()).orElseThrow();
        }
        assertEquals(State.GATHERED, instance.getState());
        assertEquals(0, instance.getGather().getExitStatus());

        // archive the instance
        instanceService.updateState(instance.getId(), State.ARCHIVING);

        // wait until archiving finishes
        do {
            Thread.sleep(100);
            instance = instanceRepository.findById(instance.getId()).orElseThrow();
        } while (instance.getState().equals(State.ARCHIVING));
        assertEquals(State.ARCHIVED, instance.getState());
        assertTrue(instanceThumbnailRepository.existsByInstanceAndType(instance, LIVE));

    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of("PANDAS_HOME=" + tempDir).applyTo(context);
        }
    }
}
