package pandas.collection;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pandas.agency.User;
import pandas.agency.UserRepository;

import java.time.Instant;

@Controller
public class ContactEventController {
    private final ContactEventRepository contactEventRepository;
    private final ContactPersonRepository contactPersonRepository;
    private final UserRepository userRepository;
    private final ContactMethodRepository contactMethodRepository;
    private final ContactTypeRepository contactTypeRepository;

    public ContactEventController(ContactEventRepository contactEventRepository,
                                 TitleRepository titleRepository,
                                 PublisherRepository publisherRepository,
                                 ContactPersonRepository contactPersonRepository,
                                 UserRepository userRepository,
                                 ContactMethodRepository contactMethodRepository,
                                 ContactTypeRepository contactTypeRepository) {
        this.contactEventRepository = contactEventRepository;
        this.contactPersonRepository = contactPersonRepository;
        this.userRepository = userRepository;
        this.contactMethodRepository = contactMethodRepository;
        this.contactTypeRepository = contactTypeRepository;
    }

    // Contact events for titles
    @GetMapping("/titles/{titleId}/contact-events/new")
    @PreAuthorize("hasPermission(#title, 'edit')")
    public String newForTitle(@PathVariable("titleId") Title title, Model model) {
        ContactEvent contactEvent = new ContactEvent();
        contactEvent.setTitle(title);
        contactEvent.setDate(Instant.now());
        
        model.addAttribute("form", ContactEventEditForm.from(contactEvent));
        model.addAttribute("title", title);
        model.addAttribute("contactPeople", title.getContactPeople());
        model.addAttribute("contactMethods", contactMethodRepository.findAllByOrderByName());
        model.addAttribute("contactTypes", contactTypeRepository.findAllByOrderByName());
        return "ContactEventEdit";
    }

    @GetMapping("/titles/{titleId}/contact-events/{id}/edit")
    @PreAuthorize("hasPermission(#title, 'edit')")
    public String editForTitle(@PathVariable("titleId") Title title,
                              @PathVariable("id") ContactEvent contactEvent,
                              Model model) {
        if (!title.equals(contactEvent.getTitle())) {
            throw new IllegalArgumentException("Contact event does not belong to this title");
        }
        
        model.addAttribute("form", ContactEventEditForm.from(contactEvent));
        model.addAttribute("title", title);
        model.addAttribute("contactPeople", title.getContactPeople());
        model.addAttribute("contactMethods", contactMethodRepository.findAllByOrderByName());
        model.addAttribute("contactTypes", contactTypeRepository.findAllByOrderByName());
        model.addAttribute("contactEventId", contactEvent.getId());
        return "ContactEventEdit";
    }

    @PostMapping("/titles/{titleId}/contact-events/new")
    @PreAuthorize("hasPermission(#title, 'edit')")
    @Transactional
    public String createForTitle(@PathVariable("titleId") Title title,
                                ContactEventEditForm form,
                                Authentication authentication,
                                Model model) {
        ContactEvent contactEvent = new ContactEvent();
        form.applyTo(contactEvent);
        contactEvent.setTitle(title);
        
        // Set current user if not specified
        if (contactEvent.getUser() == null && authentication != null && authentication.getPrincipal() instanceof User user) {
            contactEvent.setUser(user);
        }
        
        contactEventRepository.save(contactEvent);
        return "redirect:/titles/" + title.getId();
    }

    @PostMapping("/titles/{titleId}/contact-events/{id}/edit")
    @PreAuthorize("hasPermission(#title, 'edit')")
    @Transactional
    public String updateForTitle(@PathVariable("titleId") Title title,
                                @PathVariable("id") ContactEvent contactEvent,
                                ContactEventEditForm form,
                                Authentication authentication) {
        if (!title.equals(contactEvent.getTitle())) {
            throw new IllegalArgumentException("Contact event does not belong to this title");
        }
        
        form.applyTo(contactEvent);
        
        // Set current user if not specified
        if (contactEvent.getUser() == null && authentication != null && authentication.getPrincipal() instanceof User user) {
            contactEvent.setUser(user);
        }
        
        contactEventRepository.save(contactEvent);
        return "redirect:/titles/" + title.getId();
    }

    // Contact events for publishers
    @GetMapping("/publishers/{publisherId}/contact-events/new")
    @PreAuthorize("hasPermission(#publisher, 'edit')")
    public String newForPublisher(@PathVariable("publisherId") Publisher publisher, Model model) {
        ContactEvent contactEvent = new ContactEvent();
        contactEvent.setPublisher(publisher);
        contactEvent.setDate(Instant.now());
        
        model.addAttribute("form", ContactEventEditForm.from(contactEvent));
        model.addAttribute("publisher", publisher);
        model.addAttribute("contactPeople", contactPersonRepository.findByOrganisation(publisher.getOrganisation()));
        model.addAttribute("contactMethods", contactMethodRepository.findAllByOrderByName());
        model.addAttribute("contactTypes", contactTypeRepository.findAllByOrderByName());
        return "ContactEventEdit";
    }

    @GetMapping("/publishers/{publisherId}/contact-events/{id}/edit")
    @PreAuthorize("hasPermission(#publisher, 'edit')")
    public String editForPublisher(@PathVariable("publisherId") Publisher publisher,
                                  @PathVariable("id") ContactEvent contactEvent,
                                  Model model) {
        if (!publisher.equals(contactEvent.getPublisher())) {
            throw new IllegalArgumentException("Contact event does not belong to this publisher");
        }
        
        model.addAttribute("form", ContactEventEditForm.from(contactEvent));
        model.addAttribute("publisher", publisher);
        model.addAttribute("contactPeople", contactPersonRepository.findByOrganisation(publisher.getOrganisation()));
        model.addAttribute("contactMethods", contactMethodRepository.findAllByOrderByName());
        model.addAttribute("contactTypes", contactTypeRepository.findAllByOrderByName());
        model.addAttribute("contactEventId", contactEvent.getId());
        return "ContactEventEdit";
    }

    @PostMapping("/publishers/{publisherId}/contact-events/new")
    @PreAuthorize("hasPermission(#publisher, 'edit')")
    @Transactional
    public String createForPublisher(@PathVariable("publisherId") Publisher publisher,
                                    ContactEventEditForm form,
                                    Authentication authentication,
                                    Model model) {
        ContactEvent contactEvent = new ContactEvent();
        form.applyTo(contactEvent);
        contactEvent.setPublisher(publisher);
        
        // Set current user if not specified
        if (contactEvent.getUser() == null && authentication != null && authentication.getPrincipal() instanceof User user) {
            contactEvent.setUser(user);
        }
        
        contactEventRepository.save(contactEvent);
        return "redirect:/publishers/" + publisher.getId();
    }

    @PostMapping("/publishers/{publisherId}/contact-events/{id}/edit")
    @PreAuthorize("hasPermission(#publisher, 'edit')")
    @Transactional
    public String updateForPublisher(@PathVariable("publisherId") Publisher publisher,
                                    @PathVariable("id") ContactEvent contactEvent,
                                    ContactEventEditForm form,
                                    Authentication authentication) {
        if (!publisher.equals(contactEvent.getPublisher())) {
            throw new IllegalArgumentException("Contact event does not belong to this publisher");
        }
        
        form.applyTo(contactEvent);
        
        // Set current user if not specified
        if (contactEvent.getUser() == null && authentication != null && authentication.getPrincipal() instanceof User user) {
            contactEvent.setUser(user);
        }
        
        contactEventRepository.save(contactEvent);
        return "redirect:/publishers/" + publisher.getId();
    }

    @PostMapping("/contact-events/{id}/delete")
    @PreAuthorize("hasPermission(#contactEvent.title != null ? #contactEvent.title : #contactEvent.publisher, 'edit')")
    @Transactional
    public String delete(@PathVariable("id") ContactEvent contactEvent) {
        String redirectUrl = "/";
        
        if (contactEvent.getTitle() != null) {
            redirectUrl = "redirect:/titles/" + contactEvent.getTitle().getId();
        } else if (contactEvent.getPublisher() != null) {
            redirectUrl = "redirect:/publishers/" + contactEvent.getPublisher().getId();
        }
        
        contactEventRepository.delete(contactEvent);
        return redirectUrl;
    }

}