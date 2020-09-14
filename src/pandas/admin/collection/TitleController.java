package pandas.admin.collection;

import org.hibernate.search.engine.search.aggregation.AggregationKey;
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
import pandas.admin.agency.Agency;
import pandas.admin.agency.AgencyRepository;
import pandas.admin.search.Facet;
import pandas.admin.search.SearchResults;

import javax.persistence.EntityManager;
import java.util.*;

import static org.hibernate.search.engine.search.common.BooleanOperator.AND;
import static pandas.admin.search.SearchUtils.mustMatchAny;

@Controller
public class TitleController {
    private final TitleRepository titleRepository;
    private final SubjectRepository subjectRepository;
    private final AgencyRepository agencyRepository;
    private final StatusRepository statusRepository;
    private final Config config;
    private final EntityManager entityManager;

    public TitleController(TitleRepository titleRepository, SubjectRepository subjectRepository, AgencyRepository agencyRepository, StatusRepository statusRepository, Config config, EntityManager entityManager) {
        this.titleRepository = titleRepository;
        this.subjectRepository = subjectRepository;
        this.agencyRepository = agencyRepository;
        this.statusRepository = statusRepository;
        this.config = config;
        this.entityManager = entityManager;
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

        AggregationKey<Map<Long, Long>> agencyFacet = AggregationKey.of("Agency");
        AggregationKey<Map<Long, Long>> subjectFacet = AggregationKey.of("Subject");
        AggregationKey<Map<Long, Long>> statusFacet = AggregationKey.of("Status");

        var search = Search.session(entityManager).search(Title.class)
                .where(f -> f.bool(b -> {
                    b.must(f.matchAll());
                    if (q != null) b.must(f.simpleQueryString().field("name").matching(q).defaultOperator(AND));
                    mustMatchAny(f, b, "subjects.id", subjectIds);
                    mustMatchAny(f, b, "collections.id", collectionIds);
                }))
                .aggregation(agencyFacet, f -> f.terms().field("agency.id", Long.class).maxTermCount(10))
                .aggregation(statusFacet, f -> f.terms().field("status.id", Long.class).maxTermCount(10))
                .aggregation(subjectFacet, f -> f.terms().field("subjects.id", Long.class).maxTermCount(10))
                .sort(f -> q == null ? f.field("name_sort") : f.score());

        var uri = UriComponentsBuilder.fromPath("/titles");
        if (q != null) uri.queryParam("q", q);
        if (!subjectIds.isEmpty()) uri.queryParam("subject", subjectIds);
        if (!collectionIds.isEmpty()) uri.queryParam("collection", collectionIds);

        SearchResults<Title> results = SearchResults.from(search, uri, pageable);
        List<Facet> facets = List.of(
                results.facet(agencyFacet, agencyRepository::findAllById, "agency", Agency::getId, Agency::getName),
                results.facet(statusFacet, statusRepository::findAllById, "status", Status::getId, Status::getName),
                results.facet(subjectFacet, subjectRepository::findAllById, "subject", Subject::getId, Subject::getFullName));

        model.addAttribute("results", results);
        model.addAttribute("q", q);
        model.addAttribute("selectedSubjectIds", subjectIds);
        model.addAttribute("facets", facets);
        return "TitleSearch";
    }

    @GetMapping("/titles/reindex")
    @ResponseBody
    public String reindex() throws InterruptedException {
        Search.session(entityManager).massIndexer(Title.class).startAndWait();
        return "ok";
    }
}
