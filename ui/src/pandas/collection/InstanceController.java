package pandas.collection;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import static java.util.concurrent.TimeUnit.DAYS;
import static org.springframework.http.CacheControl.maxAge;

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
    @PreAuthorize("hasPermission(#instance.title, 'edit')")
    public String delete(@PathVariable("id") Instance instance) {
        if (!instance.canDelete()) throw new IllegalStateException("can't delete instance in state " + instance.getState().getName());
        instanceService.updateState(instance, State.DELETING, userService.getCurrentUser());
        return "redirect:/titles/" + instance.getTitle().getId();
    }

    @PostMapping("/instances/{id}/archive")
    @PreAuthorize("hasPermission(#instance.title, 'edit')")
    public String archive(@PathVariable("id") Instance instance) {
        if (!instance.canDelete()) throw new IllegalStateException("can't archive instance in state " + instance.getState().getName());
        instanceService.updateState(instance, State.ARCHIVING, userService.getCurrentUser());
        return "redirect:/instances/" + instance.getId();
    }

    @PostMapping("/instances/{id}/stop")
    @PreAuthorize("hasPermission(#instance.title, 'edit')")
    public String stop(@PathVariable("id") Instance instance) {
        if (instance.canStop()) {
            instanceService.updateState(instance, State.GATHERED, userService.getCurrentUser());
        }
        return "redirect:/instances/" + instance.getId();
    }

    @GetMapping("/instances/{id}/thumbnail")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable("id") Instance instance) {
        // icons from material.io/icons
        if (instance.getState().getName().equals(State.DELETED) || instance.getState().getName().equals(State.DELETING)) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml"))
                    .body("<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"24px\" viewBox=\"0 0 24 24\" width=\"24px\" fill=\"#999999\"><path d=\"M0 0h24v24H0V0z\" fill=\"none\"/><path d=\"M6 21h12V7H6v14zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z\"/></svg>".getBytes(UTF_8));
        } else if (instance.getState().getName().equals(State.GATHERED)) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml"))
                    .body("<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"24px\" viewBox=\"0 0 24 24\" width=\"24px\" fill=\"#999999\"><path d=\"M0 0h24v24H0V0z\" fill=\"none\"/><path d=\"M5 18h14v2H5v-2zm4.6-2.7L5 10.7l2-1.9 2.6 2.6L17 4l2 2-9.4 9.3z\"/></svg>".getBytes(UTF_8));
        } else if (instance.getState().getName().equals(State.FAILED)) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml"))
                    .body("<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"24px\" viewBox=\"0 0 24 24\" width=\"24px\" fill=\"#999999\"><path d=\"M0 0h24v24H0z\" fill=\"none\"/><path d=\"M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z\"/></svg>".getBytes(UTF_8));
        } else if (!instance.getState().getName().equals(State.ARCHIVED)) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml"))
                    .body("<svg xmlns=\"http://www.w3.org/2000/svg\" enable-background=\"new 0 0 24 24\" height=\"24px\" viewBox=\"0 0 24 24\" width=\"24px\" fill=\"#999999\"><g><rect fill=\"none\" height=\"24\" width=\"24\"/></g><g><path d=\"M12,2C6.48,2,2,6.48,2,12c0,5.52,4.48,10,10,10s10-4.48,10-10C22,6.48,17.52,2,12,2z M7,13.5c-0.83,0-1.5-0.67-1.5-1.5 c0-0.83,0.67-1.5,1.5-1.5s1.5,0.67,1.5,1.5C8.5,12.83,7.83,13.5,7,13.5z M12,13.5c-0.83,0-1.5-0.67-1.5-1.5 c0-0.83,0.67-1.5,1.5-1.5s1.5,0.67,1.5,1.5C13.5,12.83,12.83,13.5,12,13.5z M17,13.5c-0.83,0-1.5-0.67-1.5-1.5 c0-0.83,0.67-1.5,1.5-1.5s1.5,0.67,1.5,1.5C18.5,12.83,17.83,13.5,17,13.5z\"/></g></svg>".getBytes(UTF_8));
        }

        var thumbnail = instance.getThumbnail();
        if (thumbnail == null && System.getProperty("thumbnailOnDemand") != null && instance.getState().getName().equals(State.ARCHIVED)) {
            thumbnail = thumbnailProcessor.processAndSave(instance);
        }
        if (thumbnail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .cacheControl(maxAge(1, DAYS))
                .header("Content-Type", thumbnail.getContentType())
                .body(thumbnail.getData());
    }
}
