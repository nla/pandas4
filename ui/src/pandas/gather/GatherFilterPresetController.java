package pandas.gather;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pandas.core.NotFoundException;

@Controller
@PreAuthorize("hasAuthority('PRIV_ADMIN_GATHER_OPTIONS')")
public class GatherFilterPresetController {
    private final GatherFilterPresetRepository repository;

    public GatherFilterPresetController(GatherFilterPresetRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/gather/filterpresets/new")
    String newForm(Model model) {
        model.addAttribute("preset", new GatherFilterPreset());
        return "FilterPresetEdit";
    }

    @GetMapping("/gather/filterpresets")
    String list(Model model) {
        model.addAttribute("presets", repository.findAll());
        return "FilterPresetList";
    }

    @GetMapping("/gather/filterpresets/{id}")
    String edit(Model model, @PathVariable("id") long id) {
        model.addAttribute("preset", repository.findById(id).orElseThrow(NotFoundException::new));
        return "FilterPresetEdit";
    }

    @PostMapping("/gather/filterpresets")
    String update(GatherFilterPreset preset) {
        repository.save(preset);
        return "redirect:/gather/filterpresets";
    }

    @PostMapping("/gather/filterpresets/{id}/delete")
    String delete(@PathVariable("id") long id) {
        repository.deleteById(id);
        return "redirect:/gather/filterpresets";
    }
}
