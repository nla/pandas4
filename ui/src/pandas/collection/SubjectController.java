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
import pandas.util.MimeTypeSniffer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
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
        try (var stream = subject.getIcon().getBinaryStream()) {
            byte[] buffer = new byte[(int)Math.min(8192, subject.getIcon().length())];
            int n = stream.read(buffer);
            String mimeType = MimeTypeSniffer.sniffImageType(buffer);
            if (mimeType != null) {
                response.setContentType(mimeType);
            }
            response.setContentLengthLong(subject.getIcon().length());
            response.addHeader("Cache-Control", "max-age=86400");
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(buffer, 0, n);
            stream.transferTo(outputStream);
        }
    }
}
