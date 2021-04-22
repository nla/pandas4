package pandas.collection;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pandas.core.NotFoundException;

import java.util.Optional;

@Controller
public class SubjectController {
    private final SubjectRepository subjectRepository;

    public SubjectController(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @GetMapping("/subjects")
    public String list(Model model) {
        model.addAttribute("subjects", subjectRepository.topTwoLevels());
        return "SubjectList";
    }

    @GetMapping("/subjects/{id}")
    public String list(@PathVariable("id") Optional<Subject> subject, Model model) {
        model.addAttribute("subject", subject.orElseThrow(NotFoundException::new));
        return "SubjectView";
    }

    @GetMapping("/subjects/new")
    @PreAuthorize("hasAuthority('PRIV_EDIT_SUBJECTS')")
    public String newForm(Model model) {
        model.addAttribute("subject", new Subject());
        return "SubjectEdit";
    }

    @GetMapping("/subjects/{id}/edit")
    @PreAuthorize("hasAuthority('PRIV_EDIT_SUBJECTS')")
    public String edit(@PathVariable("id") Optional<Subject> subject, Model model) {
        model.addAttribute("subject", subject.orElseThrow(NotFoundException::new));
        return "SubjectEdit";
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasAuthority('PRIV_EDIT_SUBJECTS')")
    public String update(Subject subject) {
        subjectRepository.save(subject);
        return "redirect:/subjects/" + subject.getId();
    }

    @PostMapping("/subjects/{id}/delete")
    @PreAuthorize("hasAuthority('PRIV_EDIT_SUBJECTS')")
    public String editForm(@PathVariable("id") long id) {
        subjectRepository.deleteById(id);
        return "redirect:/subjects";
    }
}
