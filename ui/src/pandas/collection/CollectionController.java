package pandas.collection;

import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.search.mapper.orm.Search;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pandas.core.NotFoundException;
import pandas.core.View;
import pandas.gather.GatherSchedule;
import pandas.gather.GatherScheduleRepository;
import pandas.gather.GatherService;
import pandas.search.SearchResults;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import static org.hibernate.search.engine.search.common.BooleanOperator.AND;
import static pandas.core.Utils.sortBy;
import static pandas.search.SearchUtils.mustMatchAny;

@Controller
public class CollectionController {
    private final CollectionRepository collectionRepository;
    private final EntityManager entityManager;
    private final SubjectRepository subjectRepository;
    private final GatherService gatherService;
    private final GatherScheduleRepository gatherScheduleRepository;

    public CollectionController(CollectionRepository collectionRepository, EntityManager entityManager, SubjectRepository subjectRepository, GatherService gatherService, GatherScheduleRepository gatherScheduleRepository) {
        this.collectionRepository = collectionRepository;
        this.entityManager = entityManager;
        this.subjectRepository = subjectRepository;
        this.gatherService = gatherService;
        this.gatherScheduleRepository = gatherScheduleRepository;
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
                })).sort(f -> q == null ? f.field("createdDate").desc() : f.score());

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

    @GetMapping("/collections/new")
    @PreAuthorize("hasAuthority('PRIV_EDIT_COLLECTIONS')")
    public String newForm(@RequestParam(value = "parent", required = false) Collection parent,
                          @RequestParam(value = "subject", required = false) List<Subject> subjects,
                          Model model) {
        Collection collection = new Collection();
        collection.setParent(parent);
        collection.setSubjects(subjects);
        return edit(collection, model);
    }

    @GetMapping("/collections/{id}/edit")
    @PreAuthorize("hasPermission(#collection, 'edit')")
    public String edit(@PathVariable("id") Collection collection, Model model) {
        var monthNames = new LinkedHashMap<String,Integer>();
        for (var month : Month.values()) {
            monthNames.put(month.getDisplayName(TextStyle.FULL, Locale.getDefault()), month.getValue());
        }
        model.addAttribute("months", monthNames);
        model.addAttribute("collection", collection);
        model.addAttribute("form", CollectionEditForm.of(collection));
        model.addAttribute("allSubjects", sortBy(subjectRepository.findAll(), Subject::getFullName));
        model.addAttribute("allGatherSchedules", gatherService.allGatherSchedules());
        return "CollectionEdit";
    }

    @PostMapping("/collections/{id}/edit")
    @PreAuthorize("hasPermission(#collection, 'edit')")
    public String update(@PathVariable("id") Collection collection, @Valid CollectionEditForm form) {
        form.applyTo(collection);
        collection = collectionRepository.save(collection);
        return "redirect:/collections/" + collection.getId();
    }

    @PostMapping("/collections/{id}/delete")
    @PreAuthorize("hasPermission(#collection, 'edit')")
    public String delete(@PathVariable("id") Collection collection) {
        collectionRepository.delete(collection);
        return "redirect:/collections";
    }

    @PostMapping("/collections/new")
    @PreAuthorize("hasAuthority('PRIV_EDIT_COLLECTIONS')")
    public String create(@Valid CollectionEditForm form) {
        Collection collection = new Collection();
        collection.setGatherSchedule(gatherScheduleRepository.findByName(GatherSchedule.DEFAULT).orElseThrow());
        form.applyTo(collection);
        collection = collectionRepository.save(collection);
        return "redirect:/collections/" + collection.getId();
    }

    @GetMapping("/collections/reindex")
    @ResponseBody
    @PreAuthorize("hasAuthority('PRIV_SYSADMIN')")
    public String reindex() throws InterruptedException {
        Search.session(entityManager).massIndexer(Collection.class).startAndWait();
        return "OK";
    }

    @GetMapping(value = "/collections.json", produces = "application/json")
    @ResponseBody
    @JsonView(View.Summary.class)
    public Object json(@RequestParam(value = "q", required = true) String q, Pageable pageable) {
        var search = Search.session(entityManager).search(Collection.class)
                .where(f -> f.bool(b -> {
                    b.must(f.matchAll());
                    b.mustNot(f.match().field("ancestorClosed").matching(true));
                    if (q != null) b.must(f.simpleQueryString().field("fullName").matching(q).defaultOperator(AND));
                })).sort(f -> f.field("name_sort"));
        var result = search.fetch((int) pageable.getOffset(), pageable.getPageSize());
        return result.hits();
    }

}
