package pandas.collection;

import org.hibernate.search.mapper.orm.Search;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pandas.core.NotFoundException;
import pandas.search.SearchResults;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.util.List;

import static org.hibernate.search.engine.search.common.BooleanOperator.AND;
import static pandas.core.Utils.sortBy;
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
    @PreAuthorize("hasPermission(#collection, 'edit')")
    public String edit(@PathVariable("id") Collection collection, Model model) {
        model.addAttribute("collection", collection);
        model.addAttribute("allSubjects", sortBy(subjectRepository.findAll(), Subject::getFullName));
        return "CollectionEdit";
    }

    @PostMapping("/collections/{id}/delete")
    @PreAuthorize("hasPermission(#collection, 'edit')")
    public String delete(@PathVariable("id") Collection collection) {
        collectionRepository.delete(collection);
        return "redirect:/collections";
    }

    @GetMapping("/collections/new")
    @PreAuthorize("hasAuthority('PRIV_EDIT_COLLECTIONS')")
    public String newForm(@RequestParam(value = "parent", required = false) Long parentId,
                          @RequestParam(value = "subject", required = false) List<Subject> subjects,
                          Model model) {
        Collection collection = new Collection();
        collection.setSubjects(subjects);
        model.addAttribute("collection", collection);
        model.addAttribute("parentId", parentId);
        model.addAttribute("allSubjects", sortBy(subjectRepository.findAll(), Subject::getFullName));
        return "CollectionEdit";
    }

    @PostMapping("/collections")
    @PreAuthorize("hasPermission(#collection, 'edit')")
    public String update(@Valid Collection collection) {
        if (collection.getId() == null) {
            collectionRepository.save(collection);
        } else {
            Collection existing = collectionRepository.findById(collection.getId()).orElseThrow(NotFoundException::new);
            existing.setName(collection.getName());
            existing.setSubjects(collection.getSubjects());
            existing.setDescription(collection.getDescription());
            collectionRepository.save(existing);
        }
        return "redirect:/collections/" + collection.getId();
    }

    @GetMapping("/collections/reindex")
    @ResponseBody
    @PreAuthorize("hasAuthority('PRIV_SYSADMIN')")
    public String reindex() throws InterruptedException {
        Search.session(entityManager).massIndexer(Collection.class).startAndWait();
        return "OK";
    }
}
