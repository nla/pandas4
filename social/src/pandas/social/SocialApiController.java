package pandas.social;

import org.netpreserve.jwarc.WarcReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Paths;

@Controller
public class SocialApiController {
    private final Logger log = LoggerFactory.getLogger(SocialApiController.class);
    private final SocialArchiver archiver;
    private final AttachmentArchiverTask attachmentArchiverTask;
    private final SocialIndexer indexer;
    private final SocialSearcher searcher;
    private final BambooClient bambooClient;

    public SocialApiController(SocialArchiver archiver,
                               AttachmentArchiverTask attachmentArchiverTask,
                               SocialIndexer indexer, SocialSearcher searcher, BambooClient bambooClient) {
        this.archiver = archiver;
        this.attachmentArchiverTask = attachmentArchiverTask;
        this.indexer = indexer;
        this.searcher = searcher;
        this.bambooClient = bambooClient;
    }

    @GetMapping(value = "/", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String home() {
        return """
            pandas-social running
            <form action=/warcs method=post>
                <input name=filename>
                <input type=submit>
            </form>
            <p><strong>Archiver:</strong> %s
            <form method=post>
               <button type='submit' formaction='start'>Start</button>
               <button type='submit' formaction='stop'>Stop</button>
            </form>
            <p><strong>Attachment Archiver:</strong> %s
            <form method=post>
               <button type='submit' formaction='attachments/start'>Start</button>
               <button type='submit' formaction='attachments/stop'>Stop</button>
            </form>
                """.formatted(archiver.status(), attachmentArchiverTask.status());
    }

    @GetMapping(value = "/search", produces = "application/json")
    @ResponseBody
    public SocialResults search(@RequestParam("q") String query,
                                @RequestParam(value = "sort", defaultValue = "relevance") String sort) throws IOException {
        return searcher.search(query, sort);
    }

    @PostMapping(value = "/warcs")
    @ResponseBody
    public String addWarc(@RequestParam("filename") String filename) throws IOException {
        indexer.addWarc(Paths.get(filename));
        return "Added " + filename;
    }

    @GetMapping("/reindex")
    @ResponseBody
    public String reindex() throws IOException {
        long warcCount = 0;
        long postCount = 0;
        for (long warcId : bambooClient.listWarcIds()) {
            try (var warcReader = new WarcReader(bambooClient.openWarc(warcId))) {
                try {
                    postCount += indexer.addWarc(warcReader, Long.toString(warcId));
                } catch (IOException e) {
                    log.warn("Error indexing warc {} @ {}", warcId, warcReader.position(), e);
                }
                warcCount++;
            }
        }
        return "Indexed " + postCount + " posts from " + warcCount + " warcs";
    }

    @PostMapping("/start")
    public String startArchiver() {
        archiver.start();
        return "redirect:/";
    }

    @PostMapping("/stop")
    public String stopArchiver() {
        archiver.stop();
        return "redirect:/";
    }

    @PostMapping("/attachments/start")
    public String startAttachmentArchiver() {
        attachmentArchiverTask.start();
        return "redirect:/";
    }
}
