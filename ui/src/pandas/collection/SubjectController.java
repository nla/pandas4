package pandas.collection;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pandas.core.NotFoundException;
import pandas.util.ServletUtils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Controller
public class SubjectController {
    private final SubjectRepository subjectRepository;
    private final CollectionRepository collectionRepository;

    public SubjectController(SubjectRepository subjectRepository, CollectionRepository collectionRepository) {
        this.subjectRepository = subjectRepository;
        this.collectionRepository = collectionRepository;
    }

    @GetMapping("/subjects")
    public String list(Model model) {
        var level1 = new ArrayList<SubjectRepository.SubjectListItem>();
        var level2 = new HashMap<Long,List<SubjectRepository.SubjectListItem>>();
        for (var item : subjectRepository.topTwoLevels()) {
            if (item.parentId() == null) {
                level1.add(item);
                level2.computeIfAbsent(item.id(), k -> new ArrayList<>());
            } else {
                level2.computeIfAbsent(item.parentId(), k -> new ArrayList<>()).add(item);
            }
        }
        model.addAttribute("level1", level1);
        model.addAttribute("level2", level2);
        return "SubjectList";
    }

    @GetMapping("/subjects/{id}")
    public String show(@PathVariable("id") Subject subject, Model model) {
        model.addAttribute("subject", subject);
        model.addAttribute("subcategories", subjectRepository.listSubcategories(subject.getId()));
        model.addAttribute("collections", collectionRepository.listBySubject(subject));
        model.addAttribute("titleCountByCollectionId", collectionRepository.countTitlesForCollectionsInSubject(subject.getId()));
        return "SubjectView";
    }

    @GetMapping("/subjects/new")
    @PreAuthorize("hasAuthority('PRIV_EDIT_SUBJECTS')")
    public String newForm(Model model, @RequestParam("parent") Optional<Subject> parent) {
        Subject subject = new Subject();
        parent.ifPresent(subject::setParent);
        return edit(subject, model);
    }

    @GetMapping("/subjects/{id}/edit")
    @PreAuthorize("hasPermission(#subject, 'edit')")
    public String edit(@PathVariable("id") Subject subject, Model model) {
        List<Subject> allSubjects = subjectRepository.findAllByOrderByName();
        allSubjects.removeIf(s -> s.hasAncester(subject));
        model.addAttribute("allSubjects", allSubjects);
        model.addAttribute("subject", subject);
        model.addAttribute("form", SubjectEditForm.from(subject));
        return "SubjectEdit";
    }

    @PostMapping("/subjects/{id}/edit")
    @PreAuthorize("hasPermission(#subject, 'edit')")
    public String update(@PathVariable("id") Subject subject, SubjectEditForm form) throws IOException {
        form.applyTo(subject);
        subject = subjectRepository.save(subject);
        return "redirect:/subjects/" + subject.getId();
    }

    @PostMapping("/subjects/new")
    @PreAuthorize("hasAuthority('PRIV_EDIT_SUBJECTS')")
    public String create(SubjectEditForm form) throws IOException {
        Subject subject = new Subject();
        form.applyTo(subject);
        subject = subjectRepository.save(subject);
        return "redirect:/subjects/" + subject.getId();
    }

    @PostMapping("/subjects/{id}/delete")
    @PreAuthorize("hasPermission(#subject, 'edit')")
    public String editForm(@PathVariable("id") Subject subject) {
        subjectRepository.delete(subject);
        return "redirect:/subjects";
    }


    @GetMapping("/subjects/{id}/icon")
    @Transactional
    public void icon(@PathVariable("id") Subject subject, HttpServletResponse response) throws IOException, SQLException {
        if (subject.getIcon() == null) throw new NotFoundException();
        ServletUtils.sendBlobAsImage(subject.getIcon(), response);
    }
}
