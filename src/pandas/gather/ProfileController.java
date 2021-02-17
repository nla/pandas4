package pandas.gather;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class ProfileController {
    private final ProfileRepository profileRepository;

    public ProfileController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @GetMapping("/profiles")
    public String list(Model model) {
        model.addAttribute("profiles", profileRepository.findAll());
        return "gather/ProfileList";
    }

    @PostMapping("/profiles")
    public String save(@Valid Profile profile) {
        profileRepository.save(profile);
        return "redirect:/profiles";
    }

    @GetMapping("/profiles/new")
    public String newForm(Model model) {
        return edit(new Profile(), model);
    }

    @GetMapping("/profiles/{id}")
    public String edit(@PathVariable("id") Profile profile, Model model) {
        model.addAttribute("profile", profile);
        return "gather/ProfileEdit";
    }

    @PostMapping("/profiles/{id}/delete")
    public String delete(@PathVariable("id") Profile profile) {
        profileRepository.delete(profile);
        return "redirect:/profiles";
    }
}
