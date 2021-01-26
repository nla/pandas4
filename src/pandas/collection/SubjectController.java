package pandas.collection;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pandas.core.NotFoundException;

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
    public String list(@PathVariable("id") long id, Model model) {
        model.addAttribute("subject", subjectRepository.findById(id).orElseThrow(NotFoundException::new));
        return "SubjectView";
    }

    @GetMapping("/subjects/new")
    public String newForm(Model model) {
        model.addAttribute("subject", new Subject());
        return "SubjectEdit";
    }

    @GetMapping("/subjects/{id}/edit")
    public String edit(@PathVariable("id") long id, Model model) {
        model.addAttribute("subject", subjectRepository.findById(id).orElseThrow(NotFoundException::new));
        return "SubjectEdit";
    }

    @PostMapping("/subjects")
    public String update(Subject subject) {
        subjectRepository.save(subject);
        return "redirect:/subjects/" + subject.getId();
    }

    @PostMapping("/subjects/{id}/delete")
    public String editForm(@PathVariable("id") long id) {
        subjectRepository.deleteById(id);
        return "redirect:/subjects";
    }
}
