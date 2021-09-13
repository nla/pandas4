package pandas.collection;

import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.search.mapper.orm.Search;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pandas.core.View;
import pandas.search.SearchResults;

import javax.persistence.EntityManager;

import static org.hibernate.search.engine.search.common.BooleanOperator.AND;

@Controller
public class PublisherController {
    private final EntityManager entityManager;
    private final PublisherRepository publisherRepository;
    private final PublisherTypeRepository publisherTypeRepository;

    public PublisherController(EntityManager entityManager, PublisherRepository publisherRepository, PublisherTypeRepository publisherTypeRepository) {
        this.entityManager = entityManager;
        this.publisherRepository = publisherRepository;
        this.publisherTypeRepository = publisherTypeRepository;
    }

    @GetMapping("/publishers/{id}")
    public String get(@PathVariable("id") Publisher publisher, Model model) {
        model.addAttribute("publisher", publisher);
        return "PublisherView";
    }

    @GetMapping("/publishers/{id}/edit")
    @PreAuthorize("hasPermission(#publisher, 'edit')")
    public String edit(@PathVariable("id") Publisher publisher, Model model) {
        model.addAttribute("allPublisherTypes", publisherTypeRepository.findAll());
        model.addAttribute("form", PublisherEditForm.of(publisher));
        model.addAttribute("publisher", publisher);
        return "PublisherEdit";
    }

    @PostMapping("/publishers/{id}/edit")
    @PreAuthorize("hasPermission(#publisher, 'edit')")
    public String update(@PathVariable("id") Publisher publisher, PublisherEditForm form) {
        form.applyTo(publisher);
        publisher = publisherRepository.save(publisher);
        return "redirect:/publishers/" + publisher.getId();
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

    @GetMapping(value = "/publishers.json", produces = "application/json")
    @ResponseBody
    @JsonView(View.Summary.class)
    public Object json(@RequestParam(value = "q", required = true) String q, Pageable pageable) {
        var search = Search.session(entityManager).search(Publisher.class)
                .where(f -> f.bool(b -> {
                    b.must(f.matchAll());
                    if (q != null) b.must(f.simpleQueryString().field("organisation.name").matching(q).defaultOperator(AND));
                }));
        var result = search.fetch((int) pageable.getOffset(), pageable.getPageSize());
        return result.hits();
    }

    @GetMapping("/publishers/reindex")
    @ResponseBody
    @PreAuthorize("hasAuthority('PRIV_SYSADMIN')")
    public String reindex() throws InterruptedException {
        Search.session(entityManager).massIndexer(Publisher.class).startAndWait();
        return "ok";
    }
}
