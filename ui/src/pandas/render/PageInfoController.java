package pandas.render;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.io.input.BoundedInputStream;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pandas.collection.Collection;
import pandas.collection.CollectionRepository;
import pandas.collection.Subject;
import pandas.collection.SubjectRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class PageInfoController {
    private static final Logger log = LoggerFactory.getLogger(PageInfo.class);

    private final HttpClient httpClient;
    private final LoadingCache<String, PageInfo> pageInfoCache;
    private final LoadingCache<String, List<String>> subjectsCache;
    private final LLMClient llm;
    private final CollectionRepository collectionRepository;
    private final SubjectRepository subjectRepository;

    record CollectionCacheKey(String url, List<Long> subjectIds) {
    }

    public PageInfoController(@Autowired(required = false) LLMClient llm, CollectionRepository collectionRepository, SubjectRepository subjectRepository) {
        this.llm = llm;
        this.collectionRepository = collectionRepository;
        this.subjectRepository = subjectRepository;
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
                        List<String> subjects = subjectRepository.findAllSubjectNames();
                        //subjects = subjectRepository.findAllSubjectNamesNested();
                        return suggestSubjects(subjects, url, pageInfoCache.get(url), null);
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
    public List<String> getSubjectSuggestions(@RequestParam(name = "url") String url,
                                              @RequestParam(name = "nested", defaultValue = "false") boolean nested,
                                              @RequestParam(name = "cache", defaultValue = "true") boolean cache,
                                              @RequestParam(name = "prompt", required = false) String prompt) throws Exception {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("bad url");
        }

        if (nested) {
            List<String> subjects = subjectRepository.findAllSubjectNames();
            List<String> nestedSubjects = subjectRepository.findAllSubjectNamesNested();
            return suggestSubjects(subjects, url, pageInfoCache.get(url), prompt);
        }

        if (!cache) {
            List<String> subjects = subjectRepository.findAllSubjectNames();
            return suggestSubjects(subjects, url, pageInfoCache.get(url), prompt);
        }
        return subjectsCache.get(url);
    }

    @GetMapping(value = "/collections/suggest", produces = "application/json")
    @ResponseBody
    public List<SuggestedCollection> getCollectionSuggestions(@RequestParam(name = "url") String url,
                                                              @RequestParam(name = "subject") List<Subject> subjects) throws Exception {
        var collections = collectionRepository.findByAnyOfSubjects(subjects);
        var byName = collections.stream().collect(Collectors.toMap(Collection::getFullName, SuggestedCollection::new, (prev, next) -> next, TreeMap::new));
        byName.put("Other", null);
        //subjects = subjectRepository.findAllSubjectNamesNested();
        var suggestions = suggestSubjects(byName.keySet().stream().toList(), url, pageInfoCache.get(url),
                "Please classify the website below into zero, one, two or three of the following subject categories if they are relevant, making sure to select the most specific subcategory where applicable. " +
                "Output the classification as a JSON array.");
        return suggestions.stream()
                .map(byName::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public record SuggestedCollection(long id, String fullName) {
        public SuggestedCollection(Collection c) {
            this(c.getId(), c.getFullName());
        }
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
                String charsetName = mediaType.getParameter("charset");
                var boundedStream = new BoundedInputStream(body, 10 * 1024 * 1024);
                var document = Jsoup.parse(boundedStream, charsetName, url);
                return new PageInfo(response.statusCode(), reason, contentType, document);
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

    List<String> suggestSubjects(List<String> subjects, String url, PageInfo pageInfo, String instructions) {
        if (llm == null) return List.of();
        if (subjects.size() > 200) {
            subjects = subjects.subList(0, 200);
        }
        StringBuilder prompt = new StringBuilder();
//        prompt.append("Please categorise the website below into three of the following categories. Output only the categories as a JSON array of strings and no other text.\n\n<categories>\n");
        if (instructions == null) {
            instructions = "Please classify the website below into at most three of the following subject categories, making sure to select the most specific subcategory where applicable. Output the classification as a JSON array.";
        }
        prompt.append(instructions).append("\n\n\n<categories>\n");
        for (var subject: subjects) {
            prompt.append(subject).append('\n');
        }
        prompt.append("</categories>\n\n<website>\n");
        prompt.append("Title: ").append(pageInfo.getTitle()).append('\n');
        prompt.append("URL: ").append(url).append("\n\n");
        prompt.append(pageInfo.getText());
        prompt.append("\n</website>");
        try {
            var request = llm.newRequest(prompt.toString());
            request.guidedJson = Map.of("type", "array", "items", Map.of("enum", subjects));
            request.temperature = 0.0;
            request.maxTokens = 100;
            String chat = llm.chat(request).message();
            LinkedHashSet<String> suggestions = new ObjectMapper().readValue(chat, new TypeReference<>() {
            });
            return new ArrayList<>(suggestions);
        } catch (IOException e) {
            log.error("LLM returned an error", e);
            return List.of();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        var llm = new LLMClient(LLMConfig.fromEnv());
        var controller = new PageInfoController(llm, null, null);
        var pageInfo = controller.fetchPageInfo(args[0]);
        new ObjectMapper()
                .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
                .writerWithDefaultPrettyPrinter().writeValue(System.out, pageInfo);
        var subjects = Arrays.asList("""
                Aged People
                Agriculture
                Air Force
                Alternative & Complementary Health Care
                Animals
                Anthropology
                Aquaculture & Fisheries
                Archaeology
                Architecture
                Army
                Arts
                Astronomy
                Australian Republic Debate
                Banking & Finance
                Biology
                Biotechnology
                Blogs
                Business & Economy
                Centenary of Federation
                Charities and not-for-profits
                Chemistry
                Children
                Climate Change
                Comics & Zines
                Commerce
                Commonwealth Games
                Commonwealth Government
                Community Issues & Volunteering
                Computer Science
                Computers & Internet
                Conferences
                Constitution & Referenda
                Construction
                Crime & Justice
                Cultural Heritage Management
                Dance
                Decorative Arts
                Defence
                Design & Fashion
                Drug & Alcohol Issues
                Economics
                Education
                Election Campaigns
                Employment & Industrial Relations
                Energy
                Entertainment
                Environment
                Environmental Protection
                Ethnic Communities & Heritage
                Families
                Family History & Genealogy
                Family Violence
                Festivals & Events (Arts)
                Festivals & Events (Cultural)
                Film & Cinema
                Fine Arts
                Food & Drink
                Foreign Affairs & Trade
                Forestry
                Games & Hobbies
                Geography and Mapping
                Geology
                Government & Law
                Government Indigenous Policy
                Health
                Health Research
                History
                Housing
                Humanities
                Immigration & Emigration
                Indigenous Art
                Indigenous Australians
                Indigenous Business & Commerce
                Indigenous Culture
                Indigenous Education
                Indigenous Employment
                Indigenous Health
                Indigenous History
                Indigenous Land Rights
                Indigenous Languages
                Indigenous Native Title
                Indigenous Tourism
                Industrial & Manufacturing
                Industry & Technology
                Law & Regulation
                Lesbian, Gay, Bisexual, Trans and Intersex
                Libraries & Cultural Institutions
                Linguistics
                Literature
                Local Government
                Local History
                Management
                Mathematics
                Media
                Medical & Hospital Care
                Medical Conditions & Diseases
                Men
                Mental Health
                Military History
                Mining
                Multi-Media and Digital Arts
                Music
                Natural Disasters
                Navy
                Newspapers
                Olympic & Paralympic Games
                People & Culture
                People with Disabilities
                Performing Arts
                Pharmaceuticals
                Philosophy
                Photography
                Physics
                Plants
                Poetry
                Political Action
                Political Humour & Satire
                Political Parties and Politicians
                Politics
                Psychology
                Public Health
                Radio
                Religion
                Schooling
                Sciences
                Sites for Children
                Social Institutions
                Social Media
                Social Problems and Action
                Social Welfare
                Society & Social Issues
                Sociology
                Sporting Events
                Sporting Organisations
                Sporting Personalities
                Sports & Recreation
                State & Territory Government
                Taxation
                Telecommunications
                Television
                Tertiary Education
                Tourism & Travel
                Transportation
                Unit Associations
                Veterans
                Vocational Education
                Water
                Women
                Youth""".split("\n"));
        System.out.println(controller.suggestSubjects(subjects, args[0], pageInfo, null));
    }
}
