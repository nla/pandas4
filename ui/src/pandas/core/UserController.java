package pandas.core;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pandas.collection.TitleRepository;

import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Controller
public class UserController {
    private final IndividualRepository individualRepository;
    private final TitleRepository titleRepository;
    private final UserService userService;

    public UserController(IndividualRepository individualRepository, TitleRepository titleRepository, UserService userService) {
        this.individualRepository = individualRepository;
        this.titleRepository = titleRepository;
        this.userService = userService;
    }

    @GetMapping("/users/{userid}")
    public String view(@PathVariable("userid") String userid, Model model) {
        Individual user = individualRepository.findByUserid(userid).orElseThrow(NotFoundException::new);
        model.addAttribute("user", user);
        model.addAttribute("titles", titleRepository.findFirst20ByOwnerOrderByRegDateDesc(user));
        model.addAttribute("titleCount", titleRepository.countByOwner(user));
        return "UserView";
    }

    @GetMapping("/users/{userid}/edit")
    @PreAuthorize("hasPermission(#userid, 'Individual', 'edit')")
    public String edit(@PathVariable("userid") String userid, Model model) {
        Individual user = individualRepository.findByUserid(userid).orElseThrow(NotFoundException::new);
        model.addAttribute("user", user);
        model.addAttribute("form", UserEditForm.of(user));
        model.addAttribute("allowedRoles", getRoleNamesAllowedForCurrentUser());
        return "UserEdit";
    }

    @PostMapping("/users/{userid}/edit")
    @PreAuthorize("hasPermission(#collection, 'Individual', 'edit')")
    public String update(@PathVariable("userid") String userid, @Valid UserEditForm form, Authentication authentication) {
        Individual user = individualRepository.findByUserid(userid).orElseThrow(NotFoundException::new);
        if (form.roleType() != null && !getRoleNamesAllowedForCurrentUser().containsKey(form.roleType())) {
            throw new AccessDeniedException("cannot set access level higher than own level");
        }
        if (form.agency() != null && !Objects.equals(form.agency().getId(), user.getAgency().getId()) &&
                !authentication.getAuthorities().contains(Privileges.EDIT_ALL_USERS)) {
            throw new AccessDeniedException("not allowed to change agency");
        }
        form.applyTo(user);
        individualRepository.save(user);
        return "redirect:/users/" + user.getUserid();
    }

    /**
     * Returns a type to title map of the roles the current user is allowed to set, that is their level and lower.
     */
    public Map<String, String> getRoleNamesAllowedForCurrentUser() {
        String currentRoleType = userService.getCurrentUser().getRole().getType();
        var allowedRoleNames = new LinkedHashMap<String, String>();
        for (var entry : Role.titles.entrySet()) {
            allowedRoleNames.put(entry.getKey(), entry.getValue());
            if (entry.getKey().equalsIgnoreCase(currentRoleType)) {
                return allowedRoleNames;
            }
        }
        return Map.of();
    }
}
