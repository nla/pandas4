package pandas.collection;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PermissionController {
    private final PermissionRepository permissionRepository;
    private final ContactPersonRepository contactPersonRepository;

    public PermissionController(PermissionRepository permissionRepository, ContactPersonRepository contactPersonRepository) {
        this.permissionRepository = permissionRepository;
        this.contactPersonRepository = contactPersonRepository;
    }

    @GetMapping("/permissions/new")
    @PreAuthorize("hasPermission(#publisher, 'edit')")
    public String newForm(@RequestParam("publisher") Publisher publisher, Model model) {
        Permission permission = new Permission(publisher);
        model.addAttribute("form", PermissionEditForm.from(permission));
        model.addAttribute("publisher", publisher);
        model.addAttribute("contactPeople", contactPersonRepository.findByOrganisation(permission.getPublisher().getOrganisation()));
        model.addAttribute("blanket", permission.isBlanket());
        return "PermissionEdit";
    }

    @GetMapping("/permissions/{id}")
    @PreAuthorize("hasPermission(#permission.publisher, 'edit')")
    public String edit(@PathVariable("id") Permission permission, Model model) {
        model.addAttribute("form", PermissionEditForm.from(permission));
        model.addAttribute("publisher", permission.getPublisher());
        model.addAttribute("permissionId", permission.getId());
        model.addAttribute("contactPeople", contactPersonRepository.findByOrganisation(permission.getPublisher().getOrganisation()));
        model.addAttribute("blanket", permission.isBlanket());
        return "PermissionEdit";
    }

    @PostMapping("/permissions/new")
    @PreAuthorize("hasPermission(#publisher, 'edit')")
    public String create(PermissionEditForm form,
                         @RequestParam("publisher") Publisher publisher,
                         Model model) {
        Permission permission = new Permission(publisher);
        form.applyTo(permission);
        permissionRepository.save(permission);
        return "redirect:/publishers/" + publisher.getId();
    }

    @PostMapping("/permissions/{id}")
    @PreAuthorize("hasPermission(#permission.publisher, 'edit')")
    public String update(@PathVariable("id") Permission permission, PermissionEditForm form) {
        form.applyTo(permission);
        permissionRepository.save(permission);
        return "redirect:/publishers/" + permission.getPublisher().getId();
    }

    @PostMapping("/permissions/{id}/delete")
    @PreAuthorize("hasPermission(#permission.publisher, 'edit')")
    public String delete(@PathVariable("id") Permission permission) {
        Publisher publisher = permission.getPublisher();
        permissionRepository.delete(permission);
        return "redirect:/publishers/" + publisher.getId();
    }
}
