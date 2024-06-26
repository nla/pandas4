package pandas.agency;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pandas.collection.TitleRepository;
import pandas.core.NotFoundException;
import pandas.core.Privileges;
import pandas.core.Role;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Controller
public class UserController {
    private final UserRepository userRepository;
    private final TitleRepository titleRepository;
    private final UserService userService;
    private final KeycloakAdminClient keycloakAdminClient;

    public UserController(UserRepository userRepository, TitleRepository titleRepository, UserService userService,
                          @Autowired(required = false) KeycloakAdminClient keycloakAdminClient) {
        this.userRepository = userRepository;
        this.titleRepository = titleRepository;
        this.userService = userService;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @GetMapping("/users/{userid}")
    public String view(@PathVariable("userid") String userid, Model model) {
        User user = userRepository.findByUserid(userid).orElseThrow(NotFoundException::new);
        model.addAttribute("user", user);
        model.addAttribute("titles", titleRepository.findFirst20ByOwnerOrderByRegDateDesc(user));
        model.addAttribute("titleCount", titleRepository.countByOwner(user));
        return "UserView";
    }

    @GetMapping("/users/new")
    @PreAuthorize("hasPermission(#agency, 'create-user')")
    public String newForm(@RequestParam("agency") Agency agency, Model model) {
        model.addAttribute("form", UserEditForm.of(new User(agency)));
        model.addAttribute("allowedRoles", getRoleNamesAllowedForCurrentUser());
        return "UserEdit";
    }


    @PostMapping("/users/new")
    @PreAuthorize("hasPermission(#agency, 'create-user')")
    public String create(@Valid UserEditForm form, @RequestParam("agency") Agency agency, Authentication authentication) {
        User user = new User(agency);
        if (userRepository.findByUserid(form.userid()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }
        return save(form, authentication, user);
    }

    @GetMapping("/users/{userid}/edit")
    @PreAuthorize("hasPermission(#userid, 'User', 'edit')")
    public String edit(@PathVariable("userid") String userid, Model model) {
        User user = userRepository.findByUserid(userid).orElseThrow(NotFoundException::new);
        model.addAttribute("user", user);
        model.addAttribute("form", UserEditForm.of(user));
        model.addAttribute("allowedRoles", getRoleNamesAllowedForCurrentUser());
        boolean editingSelf = user.equals(userService.getCurrentUser());
        model.addAttribute("editingSelf", editingSelf);
        model.addAttribute("changePasswordViaOpenid", editingSelf && userService.loggedInViaOpenid());
        return "UserEdit";
    }

    @PostMapping("/users/{userid}/edit")
    @PreAuthorize("hasPermission(#userid, 'User', 'edit')")
    public String update(@PathVariable("userid") String userid, @Valid UserEditForm form, Authentication authentication) {
        User user = userRepository.findByUserid(userid).orElseThrow(NotFoundException::new);
        return save(form, authentication, user);
    }

    @PostMapping("/users/{userid}/delete")
    @PreAuthorize("hasPermission(#userid, 'User', 'edit')")
    public String delete(@PathVariable("userid") String userid) {
        User user = userRepository.findByUserid(userid).orElseThrow();
        userRepository.delete(user);
        return "redirect:/agencies/" + user.getAgency().getId();
    }

    private String save(UserEditForm form, Authentication authentication, User user) {
        if (form.roleType() != null && !getRoleNamesAllowedForCurrentUser().containsKey(form.roleType())) {
            throw new AccessDeniedException("cannot set access level higher than own level");
        }
        if (form.agency() != null && !Objects.equals(form.agency().getId(), user.getAgency().getId()) &&
                !authentication.getAuthorities().contains(Privileges.EDIT_ALL_USERS)) {
            throw new AccessDeniedException("not allowed to change agency");
        }
        boolean editingSelf = user.equals(userService.getCurrentUser());
        form.applyTo(user, editingSelf);

        if (keycloakAdminClient != null) {
            // If this is a new user, and we aren't creating users via the Keycloak API, we need to first save the user
            // to the database so the Keycloak plugin can see it. Otherwise, Keycloak will complain the user doesn't
            // exist when we try to set the password for the first time.
            // This can be removed when we stop using the PANDAS keycloak plugin.
            if (!keycloakAdminClient.isSavingUsersToKeycloak() && user.getId() == null) {
                userRepository.save(user);
            }

            keycloakAdminClient.saveUser(user);
            user.setPassword(null); // password is stored in Keycloak so don't save it to PANDAS DB
        }

        userRepository.save(user);
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
