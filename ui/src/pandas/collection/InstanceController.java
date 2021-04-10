package pandas.collection;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pandas.core.Config;
import pandas.core.UserService;
import pandas.gather.Instance;
import pandas.gather.InstanceService;
import pandas.gather.State;
import pandas.gather.StateHistoryRepository;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static java.nio.charset.StandardCharsets.UTF_8;

@Controller
public class InstanceController {
    private final Config config;
    private final UserService userService;
    private final InstanceService instanceService;
    private final StateHistoryRepository stateHistoryRepository;
    private final InstanceThumbnailProcessor thumbnailProcessor;

    public InstanceController(Config config, UserService userService, InstanceService instanceService, StateHistoryRepository stateHistoryRepository, InstanceThumbnailProcessor thumbnailProcessor) {
        this.config = config;
        this.userService = userService;
        this.instanceService = instanceService;
        this.stateHistoryRepository = stateHistoryRepository;
        this.thumbnailProcessor = thumbnailProcessor;
    }

    @GetMapping("/instances/{id}")
    public String get(@PathVariable("id") Instance instance, Model model) {
        model.addAttribute("bambooUrl", config.getBambooUrl() + "/instances/" + instance.getId());
        model.addAttribute("instance", instance);
        model.addAttribute("stateHistory", stateHistoryRepository.findByInstanceOrderByStartDate(instance));
        model.addAttribute("dateFormat", DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault()));
        model.addAttribute("arcDateFormat", InstanceThumbnailProcessor.ARC_DATE);
        return "InstanceView";
    }

    @PostMapping("/instances/{id}/delete")
    public String delete(@PathVariable("id") Instance instance) {
        if (!instance.canDelete()) throw new IllegalStateException("can't delete instance in state " + instance.getState().getName());
        instanceService.updateState(instance, State.DELETING, userService.getCurrentUser());
        return "redirect:/titles/" + instance.getTitle().getId();
    }

    @PostMapping("/instances/{id}/archive")
    public String archive(@PathVariable("id") Instance instance) {
        if (!instance.canDelete()) throw new IllegalStateException("can't archive instance in state " + instance.getState().getName());
        instanceService.updateState(instance, State.ARCHIVING, userService.getCurrentUser());
        return "redirect:/instances/" + instance.getId();
    }

    @PostMapping("/instances/{id}/stop")
    public String stop(@PathVariable("id") Instance instance) {
        if (instance.canStop()) {
            instanceService.updateState(instance, State.GATHERED, userService.getCurrentUser());
        }
        return "redirect:/instances/" + instance.getId();
    }

    @GetMapping("/instances/{id}/thumbnail")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable("id") Instance instance) {
        if (instance.getState().getName().equals(State.DELETED) || instance.getState().getName().equals(State.DELETING)) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml"))
                    .body("<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"24px\" viewBox=\"0 0 24 24\" width=\"24px\" fill=\"#999999\"><path d=\"M0 0h24v24H0V0z\" fill=\"none\"/><path d=\"M6 21h12V7H6v14zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z\"/></svg>".getBytes(UTF_8));
        }

        var thumbnail = instance.getThumbnail();
        if (thumbnail == null && instance.getState().getName().equals(State.ARCHIVED)) {
            thumbnail = thumbnailProcessor.processAndSave(instance);
        }
        if (thumbnail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header("Content-Type", thumbnail.getContentType())
                .body(thumbnail.getData());
    }
}
