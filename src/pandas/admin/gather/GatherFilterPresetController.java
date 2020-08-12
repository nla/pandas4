package pandas.admin.gather;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pandas.admin.core.NotFoundException;

@Controller
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

    @PostMapping("/gather/filterpresets/{id}/update")
    String update(GatherFilterPreset preset) {
        repository.save(preset);
        return "redirect:/gather/filterpresets";
    }

    @PostMapping("/gather/filterpresets/create")
    String create(GatherFilterPreset preset) {
        repository.save(preset);
        return "redirect:/gather/filterpresets";
    }

    @PostMapping("/gather/filterpresets/{id}/delete")
    String delete(@PathVariable("id") long id) {
        repository.deleteById(id);
        return "redirect:/gather/filterpresets";
    }
}
