package pandas.collection;

import org.hibernate.search.mapper.orm.Search;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pandas.core.NotFoundException;
import pandas.search.SearchResults;

import javax.persistence.EntityManager;
import java.util.List;

import static org.hibernate.search.engine.search.common.BooleanOperator.AND;
import static pandas.search.SearchUtils.mustMatchAny;

@Controller
public class CollectionController {
    private final CollectionRepository collectionRepository;
    private final EntityManager entityManager;
    private final SubjectRepository subjectRepository;

    public CollectionController(CollectionRepository collectionRepository, EntityManager entityManager, SubjectRepository subjectRepository) {
        this.collectionRepository = collectionRepository;
        this.entityManager = entityManager;
        this.subjectRepository = subjectRepository;
    }


    @GetMapping("/collections")
    public String search(@RequestParam(value = "q", required = false) String rawQ,
                          @RequestParam(value = "subject", required = false, defaultValue = "") List<Long> subjectIds,
                          Pageable pageable,
                          Model model) {
        String q = (rawQ == null || rawQ.isBlank()) ? null : rawQ;

        var search = Search.session(entityManager).search(Collection.class)
                .where(f -> f.bool(b -> {
                    b.must(f.matchAll());
                    if (q != null) b.must(f.simpleQueryString().field("name").matching(q).defaultOperator(AND));
                    mustMatchAny(f, b, "subjects.id", subjectIds);
                })).sort(f -> q == null ? f.field("name_sort") : f.score());

        var result = search.fetch((int) pageable.getOffset(), pageable.getPageSize());
        SearchResults<Collection> results = new SearchResults<>(result, null, pageable);
        model.addAttribute("results", results);
        model.addAttribute("q", q);
        model.addAttribute("selectedSubjectIds", subjectIds);
        model.addAttribute("allSubjects", subjectRepository.findAllByOrderByName());
        return "CollectionSearch";
    }

    @GetMapping("/collections/{id}")
    public String get(@PathVariable("id") long id, Model model) {
        model.addAttribute("collection", collectionRepository.findById(id).orElseThrow(NotFoundException::new));
        return "CollectionView";
    }

    @GetMapping("/collections/{id}/edit")
    public String edit(@PathVariable("id") long id, Model model) {
        model.addAttribute("collection", collectionRepository.findById(id).orElseThrow(NotFoundException::new));
        return "CollectionEdit";
    }

    @PostMapping("/collections/{id}/edit")
    public String update(@PathVariable long id, @RequestParam("name") String name,
                       @RequestParam("description") String description) {
        Collection collection = collectionRepository.findById(id).orElseThrow(NotFoundException::new);
        collection.setName(name);
        collection.setDescription(description);
        collectionRepository.save(collection);
        return "redirect:/collections/" + id;
    }

    @PostMapping("/collections/{id}/delete")
    public String delete(@PathVariable long id) {
        collectionRepository.deleteById(id);
        return "redirect:/collections";
    }

    @GetMapping("/collections/new")
    public String newForm(@RequestParam("parentId") long parentId,
                          Model model) {
        Category collection = new Collection();
        model.addAttribute("collection", collection);
        model.addAttribute("parentId", parentId);
        return "CollectionEdit";
    }

    @PostMapping("/collections/new")
    public String create(@RequestParam("parentId") long parentId,
                         @RequestParam("name") String name,
                         @RequestParam("description") String description) {
        Collection parent = collectionRepository.findById(parentId).orElseThrow(NotFoundException::new);
        Collection collection = new Collection();
        collection.setParent(parent);
        collection.setName(name);
        collection.setDescription(description);
        collectionRepository.save(collection);
        return "redirect:/collections/" + collection.getCategoryId();
    }

    @GetMapping("/collections/reindex")
    @ResponseBody
    public String reindex() throws InterruptedException {
        Search.session(entityManager).massIndexer(Collection.class).startAndWait();
        return "OK";
    }
}
