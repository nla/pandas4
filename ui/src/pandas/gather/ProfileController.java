package pandas.gather;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Controller
@PreAuthorize("hasAuthority('PRIV_ADMIN_GATHER_OPTIONS')")
public class ProfileController {
    private final ProfileRepository profileRepository;
    private final GatherMethodRepository gatherMethodRepository;

    public ProfileController(ProfileRepository profileRepository, GatherMethodRepository gatherMethodRepository) {
        this.profileRepository = profileRepository;
        this.gatherMethodRepository = gatherMethodRepository;
    }

    @GetMapping("/profiles")
    public String list(Model model) {
        model.addAttribute("profiles", profileRepository.findAllByOrderByName());
        return "gather/ProfileList";
    }

    @PostMapping("/profiles/new")
    public String create(@Valid ProfileEditForm form) {
        return update(new Profile(), form);
    }

    @PostMapping("/profiles/{id}")
    public String update(@PathVariable("id") Profile profile, @Valid ProfileEditForm form) {
        form.applyTo(profile);
        profileRepository.save(profile);
        return "redirect:/profiles";
    }

    @GetMapping("/profiles/new")
    public String newForm(Model model) {
        return edit(new Profile(), model);
    }

    @GetMapping("/profiles/{id}")
    public String edit(@PathVariable("id") Profile profile, Model model) {
        model.addAttribute("allGatherMethods", gatherMethodRepository.findAll());
        model.addAttribute("profile", profile);
        model.addAttribute("form", new ProfileEditForm(profile));
        return "gather/ProfileEdit";
    }

    @PostMapping("/profiles/{id}/delete")
    public String delete(@PathVariable("id") Profile profile) {
        profileRepository.delete(profile);
        return "redirect:/profiles";
    }

    public record ProfileEditForm(
            @NotBlank String name,
            String description,
            GatherMethod gatherMethod,
            String heritrixConfig,
            String browsertrixConfig,
            @Positive Long crawlLimitBytes,
            @Positive Long crawlLimitSeconds) {
        ProfileEditForm(Profile profile) {
            this(profile.getName(),
                    profile.getDescription(),
                    profile.getGatherMethod(),
                    profile.getHeritrixConfig(),
                    profile.getBrowsertrixConfig(),
                    profile.getCrawlLimitBytes(),
                    profile.getCrawlLimitSeconds());
        }

        void applyTo(Profile profile) {
            profile.setName(name);
            profile.setDescription(description);
            profile.setGatherMethod(gatherMethod);
            profile.setHeritrixConfig(heritrixConfig);
            profile.setBrowsertrixConfig(browsertrixConfig);
            profile.setCrawlLimitBytes(crawlLimitBytes);
            profile.setCrawlLimitSeconds(crawlLimitSeconds);
        }
    }
}
