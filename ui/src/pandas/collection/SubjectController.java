package pandas.collection;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pandas.core.NotFoundException;

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
        model.addAttribute("subjects", subjectRepository.topTwoLevels());
        return "SubjectList";
    }

    @GetMapping("/subjects/{id}")
    public String show(@PathVariable("id") Subject subject, Model model) {
        model.addAttribute("subject", subject);
        model.addAttribute("collections", collectionRepository.findByParentIsNullAndSubjectsContainsOrderByName(subject));
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
    @PreAuthorize("hasAuthority('PRIV_EDIT_SUBJECTS')")
    public String edit(@PathVariable("id") Subject subject, Model model) {
        List<Subject> allSubjects = subjectRepository.findAllByOrderByName();
        allSubjects.removeIf(s -> s.hasAncester(subject));
        model.addAttribute("allSubjects", allSubjects);
        model.addAttribute("subject", subject);
        return "SubjectEdit";
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasAuthority('PRIV_EDIT_SUBJECTS')")
    public String update(Subject form) {
        Subject subject;
        if (form.getId() == null) {
            subject = new Subject();
        } else {
            subject = subjectRepository.findById(form.getId()).orElseThrow(NotFoundException::new);
        }
        subject.setParent(form.getParent());
        subject.setName(form.getName());
        subject.setDescription(form.getDescription());
        subject = subjectRepository.save(subject);
        return "redirect:/subjects/" + subject.getId();
    }

    @PostMapping("/subjects/{id}/delete")
    @PreAuthorize("hasAuthority('PRIV_EDIT_SUBJECTS')")
    public String editForm(@PathVariable("id") long id) {
        subjectRepository.deleteById(id);
        return "redirect:/subjects";
    }
}
