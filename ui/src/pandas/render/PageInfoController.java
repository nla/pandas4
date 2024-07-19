package pandas.render;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.attoparser.MarkupParser;
import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;
import org.attoparser.discard.DiscardMarkupHandler;
import org.attoparser.select.BlockSelectorMarkupHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pandas.collection.SubjectRepository;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class PageInfoController {
    private static final Logger log = LoggerFactory.getLogger(PageInfo.class);

    private final HttpClient httpClient;
    private final LoadingCache<String, PageInfo> pageInfoCache;
    private final LoadingCache<String, List<String>> subjectsCache;
    private final LLMClient llm;

    public PageInfoController(@Autowired(required = false) LLMClient llm, SubjectRepository subjectRepository) {
        this.llm = llm;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        this.pageInfoCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .weigher((String url, PageInfo pageInfo) -> 32 + url.length() + pageInfo.weight())
                .maximumWeight(20000000) // roughly 20 MB
                .build(new CacheLoader<>() {
                    @Override
                    public PageInfo load(String url) throws IOException, InterruptedException {
                        return fetchPageInfo(url);
                    }
                });
        this.subjectsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public List<String> load(String url) throws Exception {
                        return suggestSubjects(subjectRepository.findAllSubjectNames(), url, pageInfoCache.get(url));
                    }
                });
    }

    @GetMapping(value = "/pageinfo", produces = "application/json")
    @ResponseBody
    public PageInfo load(@RequestParam(name = "url") String url) throws Exception {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("bad url");
        }
        return pageInfoCache.get(url);
    }

    @GetMapping(value = "/subjects/suggest", produces = "application/json")
    @ResponseBody
    public List<String> getSubjectSuggestions(@RequestParam(name = "url") String url) throws Exception {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("bad url");
        }
        return subjectsCache.get(url);
    }

    @NotNull
    private PageInfo fetchPageInfo(String url) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "nla.gov.au_bot (National Library of Australia Legal Deposit Request; +https://www.nla.gov.au/legal-deposit/request)")
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream body = response.body()) {
            String contentType = response.headers().firstValue("Content-Type").orElse("application/octet-stream");
            MediaType mediaType = MediaType.parseMediaType(contentType);
            Charset charset = mediaType.getCharset();
            String title = null;
            String text = null;
            HttpStatus status = HttpStatus.resolve(response.statusCode());
            String reason = status == null ? null : status.getReasonPhrase();
            if (mediaType.equalsTypeAndSubtype(MediaType.TEXT_HTML) && body != null) {
                PageInfo.TitleHandler handler = new PageInfo.TitleHandler();

                InputStream stream = body;
                // if there was no charset in the Content-Type header, probe for meta tags near the top of the file
                if (charset == null) {
                    BufferedInputStream bis = new BufferedInputStream(stream);
                    String charsetName = HtmlCharset.detect(bis);
                    if (charsetName != null) {
                        try {
                            charset = Charset.forName(charsetName);
                        } catch (UnsupportedCharsetException e) {
                            log.warn("Unsupported charset {}, defaulting to iso-8859-1", charsetName);
                            charset = StandardCharsets.ISO_8859_1;
                        }
                    }
                    stream = bis;
                }

                try {
                    new MarkupParser(ParseConfiguration.htmlConfiguration()).parse(new InputStreamReader(stream, charset),
                            new BlockSelectorMarkupHandler(new DiscardMarkupHandler(), handler,
                                    new String[]{"script", "noscript", "style"}));
                } catch (ParseException e) {
                    log.warn("Exception parsing " + url, e);
                }
                title = handler.title.replaceAll("\\s\\s+", " ").trim();
                if (title.length() > 1000) {
                    title = title.substring(0, 1000) + "...";
                }
                text = handler.textBuffer.toString();
            }
            String location = null;
            if (response.previousResponse().isPresent()) {
                location = response.uri().toString();
            }
            return new PageInfo(response.statusCode(), reason, contentType, charset == null ? null : charset.name(),
                    title, location, text);
        } catch (UnknownHostException e) {
            return new PageInfo(-1, e.getMessage(), null, null, null, null, null);
        }
    }

    List<String> suggestSubjects(List<String> subjects, String url, PageInfo pageInfo) {
        if (llm == null) return List.of();
        StringBuilder prompt = new StringBuilder();
        prompt.append("Please categorise the website below into three of the following categories. Output only the categories as a JSON array of strings and no other text.\n\n<categories>\n");
        for (var subject: subjects) {
            prompt.append(subject).append('\n');
        }
        prompt.append("</categories>\n\n<website>\n");
        prompt.append("Title: ").append(pageInfo.getTitle()).append('\n');
        prompt.append("URL: ").append(url).append("\n\n");
        prompt.append(pageInfo.getText());
        prompt.append("\n</website>");
        try {
            String chat = llm.chat(prompt.toString(), Map.of("type", "array",
                    "items", Map.of("enum", subjects)));
            return new ObjectMapper().readValue(chat, new TypeReference<List<String>>() {
            });
        } catch (IOException e) {
            log.error("LLM returned an error", e);
            return List.of();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        var llm = new LLMClient(LLMConfig.fromEnv());
        var controller = new PageInfoController(llm, null);
        var pageInfo = controller.fetchPageInfo(args[0]);
        new ObjectMapper()
                .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
                .writerWithDefaultPrettyPrinter().writeValue(System.out, pageInfo);
        var subjects = List.of("Arts",
                "Business & Economy",
                "Computers & Internet",
                "Cultural Institutions",
                "Education",
                "Housing",
                "Media",
                "Medicine",
                "News",
                "Libraries",
                "Literature",
                "Sciences");
        System.out.println(controller.suggestSubjects(subjects, args[0], pageInfo));
    }
}
