package pandas;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pandas.collection.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Controller
public class NominationController {
    private final SubjectRepository subjectRepository;
    private final CollectionRepository collectionRepository;
    private final TitleRepository titleRepository;

    public NominationController(SubjectRepository subjectRepository, CollectionRepository collectionRepository, TitleRepository titleRepository) {
        this.subjectRepository = subjectRepository;
        this.collectionRepository = collectionRepository;
        this.titleRepository = titleRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/saveasite";
    }

    @GetMapping("/saveasite")
    public String form(Model model) {
        var allSubjects = new ArrayList<Subject>();
        subjectRepository.findAll().forEach(allSubjects::add);
        allSubjects.sort(Comparator.comparing(Subject::getFullName));
        model.addAttribute("allSubjects", allSubjects);
        return "NominationForm";
    }

    @GetMapping(value = "/saveasite/collections.json", produces = "application/json")
    @ResponseBody
    public List<OptionGroup> collectionsJson(@RequestParam(value = "subject", required = false) List<Subject> subjects) {
        var groups = new ArrayList<OptionGroup>();
        for (var subject : subjects) {
            ArrayList<Option> options = new ArrayList<>();
            for (var collection : subject.getCollections()) {
                options.add(new Option(collection.getName(), Long.toString(collection.getId())));
            }
            groups.add(new OptionGroup(subject.getName(), options));
        }
        return groups;
    }

    @GetMapping(value = "/saveasite/check.json", produces = "application/json")
    @ResponseBody
    public Optional<TitleInfo> checkJson(@RequestParam(value = "url") String url) {
        // FIXME: put canonicalised urls into the database so we don't have to try lots of variants
        var variants = new ArrayList<String>();
        variants.add(url);
        var baseUrl = url.replaceFirst("https?://", "");
        for (var prefix : List.of("http://", "https://", "http://www.", "https://www.")) {
            variants.add(prefix + baseUrl);
            variants.add(prefix + baseUrl + "/");
            variants.add(prefix + baseUrl + "/index.html");
        }
        var titles = titleRepository.findByTitleUrlIn(variants);
        if (titles.isEmpty()) return Optional.empty();
        return Optional.of(new TitleInfo(titles.get(0)));
    }

    /**
     * Subset of the title information we want to return.
     */
    public static class TitleInfo {
        private final Title title;

        public TitleInfo(Title title) {
            this.title = title;
        }

        public Long getPi() { return title.getPi(); }
        public String getName() { return title.getName(); }
        public List<Long> getSubjectIds() { return title.getSubjects().stream().map(Subject::getId).collect(toList());}
        public List<Long> getCollectionIds() { return title.getCollections().stream().map(Collection::getId).collect(toList());}
    }

    @GetMapping(value = "/saveasite/check2.json", produces = "application/json")
    @ResponseBody
    public DocInfo check2Json(@RequestParam(value = "url") String url) throws IOException {
        if (!url.startsWith("https://") && !url.startsWith("http://")) throw new IllegalArgumentException("invalid url");
        var doc = Jsoup.connect(url).timeout(10000).maxBodySize(10 * 1024 * 1024).get();
        return new DocInfo(doc);
    }

    public static class DocInfo {
        private final Document document;

        public DocInfo(Document document) {
            this.document = document;
        }

        public String getTitle() {
            return document.title();
        }
    }

    public static class Option {
        public final String text;
        public final String value;

        public Option(String text, String value) {
            this.text = text;
            this.value = value;
        }
    }

    public static class OptionGroup {
        public final String label;
        public final List<Option> options;

        public OptionGroup(String label, List<Option> options) {
            this.label = label;
            this.options = options;
        }
    }
}
