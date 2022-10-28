package pandas.collection;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactPersonController {
    private final ContactPersonRepository contactPersonRepository;

    public ContactPersonController(ContactPersonRepository contactPersonRepository, PublisherRepository publisherRepository) {
        this.contactPersonRepository = contactPersonRepository;
    }

    @GetMapping("/contact-people/new")
    @PreAuthorize("hasPermission(#publisher, 'edit')")
    public String newForm(@RequestParam("publisher") Publisher publisher, Model model) {
        ContactPerson contactPerson = new ContactPerson();
        model.addAttribute("form", ContactPersonEditForm.from(contactPerson));
        model.addAttribute("publisher", publisher);
        return "ContactPersonEdit";
    }

    @GetMapping("/contact-people/{id}/edit")
    @PreAuthorize("hasPermission(#contactPerson.publisher, 'edit')")
    public String edit(@PathVariable("id") ContactPerson contactPerson, Model model) {
        model.addAttribute("form", ContactPersonEditForm.from(contactPerson));
        model.addAttribute("publisher", contactPerson.getPublisher());
        model.addAttribute("contactPersonId", contactPerson.getId());
        return "ContactPersonEdit";
    }

    @PostMapping("/contact-people/new")
    @PreAuthorize("hasPermission(#publisher, 'edit')")
    public String create(ContactPersonEditForm form,
                         @RequestParam("publisher") Publisher publisher,
                         Model model) {
        ContactPerson contactPerson = new ContactPerson();
        form.applyTo(contactPerson);
        contactPerson.setPublisher(publisher);
        contactPersonRepository.save(contactPerson);
        return "redirect:/publishers/" + publisher.getId();
    }

    @PostMapping("/contact-people/{id}/edit")
    @PreAuthorize("hasPermission(#contactPerson.publisher, 'edit')")
    public String update(@PathVariable("id") ContactPerson contactPerson, ContactPersonEditForm form) {
        form.applyTo(contactPerson);
        contactPersonRepository.save(contactPerson);
        return "redirect:/publishers/" + contactPerson.getPublisher().getId();
    }

    @PostMapping("/contact-people/{id}/delete")
    @PreAuthorize("hasPermission(#contactPerson.publisher, 'edit')")
    public String delete(@PathVariable("id") ContactPerson contactPerson) {
        Publisher publisher = contactPerson.getPublisher();
        contactPersonRepository.delete(contactPerson);
        return "redirect:/publishers/" + publisher.getId();
    }
}
