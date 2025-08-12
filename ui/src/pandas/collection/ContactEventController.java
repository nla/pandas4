package pandas.collection;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pandas.agency.User;
import pandas.agency.UserService;
import pandas.core.Resolver;

import java.time.Instant;
import java.util.List;

@Controller
public class ContactEventController {
    private final Resolver resolver;
    private final ContactEventRepository contactEventRepository;
    private final ContactPersonRepository contactPersonRepository;
    private final ContactMethodRepository contactMethodRepository;
    private final ContactTypeRepository contactTypeRepository;
    private final TitleRepository titleRepository;
    private final UserService userService;

    public ContactEventController(ContactEventRepository contactEventRepository,
                                  TitleRepository titleRepository,
                                  Resolver resolver,
                                  ContactPersonRepository contactPersonRepository,
                                  ContactMethodRepository contactMethodRepository,
                                  ContactTypeRepository contactTypeRepository,
                                  UserService userService) {
        this.contactEventRepository = contactEventRepository;
        this.titleRepository = titleRepository;
        this.resolver = resolver;
        this.contactPersonRepository = contactPersonRepository;
        this.contactMethodRepository = contactMethodRepository;
        this.contactTypeRepository = contactTypeRepository;
        this.userService = userService;
    }

    // Contact events for titles
    @GetMapping("/titles/{titleId}/contact-events/new")
    @PreAuthorize("hasPermission(#title, 'edit')")
    public String newForTitle(@PathVariable("titleId") Title title,
                              @RequestParam(required = false) ContactType type,
                              Model model) {
        ContactEvent contactEvent = new ContactEvent();
        contactEvent.setType(type);
        contactEvent.setTitle(title);
        contactEvent.setDate(Instant.now());
        
        model.addAttribute("form", ContactEventEditForm.from(contactEvent));
        model.addAttribute("title", title);
        model.addAttribute("publisher", title.getPublisher());
        model.addAttribute("publisherContacts", title.getPublisher() == null ? List.of() : contactPersonRepository.findByOrganisation(title.getPublisher().getOrganisation()));
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
        model.addAttribute("publisher", title.getPublisher());
        model.addAttribute("publisherContacts", title.getPublisher() == null ? List.of() : contactPersonRepository.findByOrganisation(title.getPublisher().getOrganisation()));
        model.addAttribute("contactMethods", contactMethodRepository.findAllByOrderByName());
        model.addAttribute("contactTypes", contactTypeRepository.findAllByOrderByName());
        model.addAttribute("contactEventId", contactEvent.getId());
        return "ContactEventEdit";
    }

    @PostMapping("/titles/{titleId}/contact-events/new")
    @PreAuthorize("hasPermission(#title, 'edit')")
    @Transactional
    public String createForTitle(@PathVariable("titleId") Title title,
                                @Valid ContactEventEditForm form,
                                Authentication authentication,
                                Model model) {
        ContactEvent contactEvent = new ContactEvent();
        form.applyTo(contactEvent, resolver);
        contactEvent.setTitle(title);
        contactEvent.setUser(userService.getCurrentUser());

        handleNewContactPersonCreation(contactEvent, form, title, title.getPublisher());

        contactEventRepository.save(contactEvent);
        return "redirect:/titles/" + title.getId();
    }

    @PostMapping("/titles/{titleId}/contact-events/{id}/edit")
    @PreAuthorize("hasPermission(#title, 'edit')")
    @Transactional
    public String updateForTitle(@PathVariable("titleId") Title title,
                                @PathVariable("id") ContactEvent contactEvent,
                                @Valid ContactEventEditForm form,
                                Authentication authentication) {
        if (!title.equals(contactEvent.getTitle())) {
            throw new IllegalArgumentException("Contact event does not belong to this title");
        }
        
        form.applyTo(contactEvent, resolver);

        handleNewContactPersonCreation(contactEvent, form, title, null);
        
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
        model.addAttribute("publisherContacts", contactPersonRepository.findByOrganisation(publisher.getOrganisation()));
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
        model.addAttribute("publisherContacts", contactPersonRepository.findByOrganisation(publisher.getOrganisation()));
        model.addAttribute("contactMethods", contactMethodRepository.findAllByOrderByName());
        model.addAttribute("contactTypes", contactTypeRepository.findAllByOrderByName());
        model.addAttribute("contactEventId", contactEvent.getId());
        return "ContactEventEdit";
    }

    @PostMapping("/publishers/{publisherId}/contact-events/new")
    @PreAuthorize("hasPermission(#publisher, 'edit')")
    @Transactional
    public String createForPublisher(@PathVariable("publisherId") Publisher publisher,
                                    @Valid ContactEventEditForm form,
                                    Authentication authentication,
                                    Model model) {
        ContactEvent contactEvent = new ContactEvent();
        form.applyTo(contactEvent, resolver);
        contactEvent.setPublisher(publisher);
        contactEvent.setUser(userService.getCurrentUser());
        
        handleNewContactPersonCreation(contactEvent, form, null, publisher);
        
        contactEventRepository.save(contactEvent);
        return "redirect:/publishers/" + publisher.getId();
    }

    @PostMapping("/publishers/{publisherId}/contact-events/{id}/edit")
    @PreAuthorize("hasPermission(#publisher, 'edit')")
    @Transactional
    public String updateForPublisher(@PathVariable("publisherId") Publisher publisher,
                                    @PathVariable("id") ContactEvent contactEvent,
                                    @Valid ContactEventEditForm form,
                                    Authentication authentication) {
        if (!publisher.equals(contactEvent.getPublisher())) {
            throw new IllegalArgumentException("Contact event does not belong to this publisher");
        }
        
        form.applyTo(contactEvent, resolver);
        
        handleNewContactPersonCreation(contactEvent, form, null, publisher);

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

    private void handleNewContactPersonCreation(ContactEvent contactEvent, ContactEventEditForm form, Title title, Publisher publisher) {
        // Handle new title contact person
        if (form.newTitleContact() != null && !form.newTitleContact().isNameBlank() && title != null) {
            ContactPerson newContact = form.newTitleContact().build();
            title.getContactPeople().add(newContact);
            contactPersonRepository.save(newContact);
            titleRepository.save(title);
            contactEvent.setContactPerson(newContact);
        }
        // Handle new publisher contact person
        else if (form.newPublisherContact() != null && !form.newPublisherContact().isNameBlank() && publisher != null) {
            ContactPerson newContact = form.newPublisherContact().build();
            newContact.setPublisher(publisher);
            contactPersonRepository.save(newContact);
            contactEvent.setContactPerson(newContact);
        }
    }

}