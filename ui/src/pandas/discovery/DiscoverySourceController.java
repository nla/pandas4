package pandas.discovery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pandas.core.View;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@Controller
@RequestMapping("/discovery-sources")
@PreAuthorize("hasAuthority('PRIV_EDIT_DISCOVERY_SOURCES')")
public class DiscoverySourceController {
    private final DiscoveryRepository discoveryRepository;
    private final DiscoverySourceRepository discoverySourceRepository;

    public DiscoverySourceController(DiscoveryRepository discoveryRepository, DiscoverySourceRepository discoverySourceRepository) {
        this.discoveryRepository = discoveryRepository;
        this.discoverySourceRepository = discoverySourceRepository;
    }

    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("sources", discoverySourceRepository.findAll());
        return "discovery/DiscoverySourceList";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("source", null);
        model.addAttribute("form", DiscoverySourceForm.blank());
        return "discovery/DiscoverySourceEdit";
    }

    @PostMapping("/new")
    public String create(@Valid DiscoverySourceForm form) {
        return update(new DiscoverySource(), form);
    }

    @GetMapping("/{id}")
    public String update(@PathVariable("id") DiscoverySource source, Model model) {
        model.addAttribute("source", source);
        model.addAttribute("form", source.toForm());
        return "discovery/DiscoverySourceEdit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") DiscoverySource source, @Valid DiscoverySourceForm form) {
        source.update(form);
        discoverySourceRepository.save(source);
        return "redirect:/discovery-sources";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") DiscoverySource source) {
        discoverySourceRepository.delete(source);
        return "redirect:/discovery-sources";
    }

    @GetMapping(value = "/{id}/dryrun", produces = "text/plain")
    public void dryrun(@PathVariable("id") DiscoverySource source, HttpServletResponse response) throws IOException, InterruptedException {
        var objectMapper = new ObjectMapper();
        objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        var writer = response.getWriter();
        var spider = new DiscoverySpider(source, discovery -> {
            try {
                writer.println(objectMapper.writerWithView(View.Summary.class)
                        .writeValueAsString(discovery));
                writer.flush();
            } catch (JsonProcessingException e) {
                e.printStackTrace(writer);
            }
        });
        spider.run();
    }

    @GetMapping(value = "/{id}/run", produces = "text/plain")
    public void run(@PathVariable("id") DiscoverySource source,
                    @RequestParam(value = "dry", defaultValue = "false") boolean dry,
                    HttpServletResponse response) throws IOException, InterruptedException {
        var objectMapper = new ObjectMapper();
        objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        var writer = response.getWriter();
        var spider = new DiscoverySpider(source, discovery -> {
            if (!dry && discoveryRepository.findBySourceAndUrl(source, discovery.getUrl()).isEmpty()) {
                discovery = discoveryRepository.save(discovery);
            }
            try {
                writer.println(objectMapper.writerWithView(View.Summary.class)
                        .writeValueAsString(discovery));
                writer.flush();
            } catch (JsonProcessingException e) {
                e.printStackTrace(writer);
            }
        });
        spider.run();
    }
}
