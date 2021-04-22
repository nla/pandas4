package pandas.collection;

import org.hibernate.search.mapper.orm.Search;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pandas.search.SearchResults;

import javax.persistence.EntityManager;

import static org.hibernate.search.engine.search.common.BooleanOperator.AND;

@Controller
public class PublisherController {
    private final EntityManager entityManager;

    public PublisherController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @GetMapping("/publishers/{id}")
    public String get(@PathVariable("id") Publisher publisher, Model model) {
        model.addAttribute("publisher", publisher);
        return "PublisherView";
    }

    @GetMapping("/publishers")
    public String search(@RequestParam(value = "q", required = false) String rawQ, Pageable pageable, Model model) {
        String q = (rawQ == null || rawQ.isBlank()) ? null : rawQ;
        var search = Search.session(entityManager).search(Publisher.class)
                .where(f -> f.bool(b -> {
                    b.must(f.matchAll());
                    if (q != null) b.must(f.simpleQueryString().field("organisation.name").matching(q).defaultOperator(AND));
                })).sort(f -> q == null ? f.field("name_sort") : f.score());
        var result = search.fetch((int) pageable.getOffset(), pageable.getPageSize());
        SearchResults<Publisher> results = new SearchResults<>(result, null, pageable);
        model.addAttribute("results", results);
        model.addAttribute("q", q);
        return "PublisherSearch";
    }

    @GetMapping("/publishers/reindex")
    @ResponseBody
    @PreAuthorize("hasAuthority('PRIV_SYSADMIN')")
    public String reindex() throws InterruptedException {
        Search.session(entityManager).massIndexer(Publisher.class).startAndWait();
        return "ok";
    }
}
