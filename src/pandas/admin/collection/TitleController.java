package pandas.admin.collection;

import org.hibernate.search.mapper.orm.Search;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;
import pandas.admin.Config;
import pandas.admin.core.NotFoundException;
import pandas.admin.core.SearchResults;
import pandas.admin.render.Render;
import pandas.admin.render.RenderService;

import javax.persistence.EntityManager;

import java.util.List;

import static org.hibernate.search.engine.search.common.BooleanOperator.AND;
import static pandas.admin.core.SearchUtils.mustMatchAny;

@Controller
public class TitleController {
    private final TitleRepository titleRepository;
    private final SubjectRepository subjectRepository;
    private final Config config;
    private final EntityManager entityManager;
    private final RenderService renderService;

    public TitleController(TitleRepository titleRepository, SubjectRepository subjectRepository, Config config, EntityManager entityManager, RenderService renderService) {
        this.titleRepository = titleRepository;
        this.subjectRepository = subjectRepository;
        this.config = config;
        this.entityManager = entityManager;
        this.renderService = renderService;
    }

    @GetMapping("/titles/{id}")
    public RedirectView get(@PathVariable("id") long id) {
        return new RedirectView(config.managementDirectActionUrl("titleView?id=" + id));
    }

    @GetMapping("/titles")
    public String search(@RequestParam(name = "q", required = false) String rawQ,
                         @RequestParam(name = "collection", defaultValue = "") List<Long> collectionIds,
                         @RequestParam(name = "subject", defaultValue = "") List<Long> subjectIds,
                         @PageableDefault(40) Pageable pageable,
                         Model model) {
        String q = (rawQ == null || rawQ.isBlank()) ? null : rawQ;

        var search = Search.session(entityManager).search(Title.class)
                .where(f -> f.bool(b -> {
                    b.must(f.matchAll());
                    if (q != null) b.must(f.simpleQueryString().field("name").matching(q).defaultOperator(AND));
                    mustMatchAny(f, b, "subjects.id", subjectIds);
                    mustMatchAny(f, b, "collections.id", collectionIds);
                })).sort(f -> q == null ? f.field("name_sort") : f.score());

        var uri = UriComponentsBuilder.fromPath("/titles");
        if (q != null) uri.queryParam("q", q);
        if (!subjectIds.isEmpty()) uri.queryParam("subject", subjectIds);
        if (!collectionIds.isEmpty()) uri.queryParam("collection", collectionIds);

        model.addAttribute("results", SearchResults.from(search, uri, pageable));
        model.addAttribute("q", q);
        model.addAttribute("allSubjects", subjectRepository.findAllByOrderByName());
        model.addAttribute("selectedSubjectIds", subjectIds);
        return "TitleSearch";
    }

    @GetMapping("/titles/reindex")
    @ResponseBody
    public String reindex() throws InterruptedException {
        Search.session(entityManager).massIndexer(Title.class).startAndWait();
        return "ok";
    }

    @GetMapping("/titles/{id}/thumbnail")
    @SuppressWarnings("unchecked")
    @ResponseBody
    public Render thumbnail(@PathVariable("id") long id, Model model) {
        Title title = titleRepository.findById(id).orElseThrow(NotFoundException::new);
        return renderService.render("https://web.archive.org.au/awa-nobanner/20080718175912/" + title.getTitleUrl());

//            model.addAttribute("images", images);
//            return "TitleThumbnail";
    }

    @GetMapping("/titles/{id}/render.json")
    @SuppressWarnings("unchecked")
    @ResponseBody
    public Render thumbnailJson(@PathVariable("id") long id, Model model) {
        Title title = titleRepository.findById(id).orElseThrow(NotFoundException::new);
        return renderService.render("https://web.archive.org.au/awa-nobanner/20080718175912/" + title.getTitleUrl());
    }
}
