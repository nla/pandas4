package pandas.gather;

import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import pandas.agency.*;
import pandas.collection.TitleRepository;
import pandas.core.Config;
import pandas.search.FileSeacher;
import pandas.util.DateFormats;
import pandas.util.Requests;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.springframework.http.CacheControl.maxAge;
import static pandas.gather.InstanceThumbnail.Type.LIVE;
import static pandas.gather.InstanceThumbnail.Type.REPLAY;

@Controller
public class InstanceController {
    private final Config config;
    private final UserService userService;
    private final InstanceService instanceService;
    private final InstanceRepository instanceRepository;
    private final InstanceThumbnailRepository instanceThumbnailRepository;
    private final StateHistoryRepository stateHistoryRepository;
    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;

    public InstanceController(Config config, UserService userService, InstanceService instanceService, InstanceRepository instanceRepository, StateHistoryRepository stateHistoryRepository, AgencyRepository agencyRepository, UserRepository userRepository, TitleRepository titleRepository, InstanceThumbnailRepository instanceThumbnailRepository) {
        this.config = config;
        this.userService = userService;
        this.instanceService = instanceService;
        this.instanceRepository = instanceRepository;
        this.stateHistoryRepository = stateHistoryRepository;
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
        this.instanceThumbnailRepository = instanceThumbnailRepository;
    }

    @GetMapping("/instances/{id}")
    public String get(@PathVariable("id") Instance instance, Model model) {
        model.addAttribute("bambooUrl", config.getBambooUrl() + "/instances/" + instance.getId());
        model.addAttribute("instance", instance);
        model.addAttribute("stateHistory", stateHistoryRepository.findByInstanceOrderByStartDate(instance));
        model.addAttribute("dateFormat", DateFormats.DAY_DATE_TIME);
        model.addAttribute("arcDateFormat", InstanceThumbnailProcessor.ARC_DATE);
        return "InstanceView";
    }

    @GetMapping("/instances/{id}/process")
    public String process(@PathVariable("id") Instance instance, @RequestParam(value = "worktray", required = false) String worktray, Model model) {
        model.addAttribute("instance", instance);
        model.addAttribute("dateFormat", DateFormats.DAY_DATE_TIME);
        model.addAttribute("worktray", worktray);
        List<Instance> recentGathers = instanceRepository.findRecentGathers(instance.getTitle(), PageRequest.of(0, 5));
        model.addAttribute("previousInstances", recentGathers.isEmpty() ? recentGathers : recentGathers.subList(1, recentGathers.size()));

        if (worktray != null) {
            Agency agency = agencyRepository.findByAlias(worktray).orElse(null);
            User user = userRepository.findByUserid(worktray).orElse(null);
            Page<Instance> prev = instanceRepository.prevInGatheredWorktray(agency, user, instance.getId(), PageRequest.of(0, 1));
            Page<Instance> next = instanceRepository.nextInGatheredWorktray(agency, user, instance.getId(), PageRequest.of(0, 1));
            model.addAttribute("worktrayPosition", prev.getTotalElements() + 1);
            model.addAttribute("worktrayLength", prev.getTotalElements() + next.getTotalElements() + 1);
            model.addAttribute("prevInstance", prev.isEmpty() ? null : prev.iterator().next());
            model.addAttribute("nextInstance", next.isEmpty() ? null : next.iterator().next());
        }

        return "InstanceProcess";
    }

    @PostMapping("/instances/{id}/delete")
    @PreAuthorize("hasPermission(#instance.title, 'edit')")
    public String delete(@PathVariable("id") Instance instance,
                         @RequestParam(value = "nextInstance", required = false) Long nextInstance,
                         @RequestParam(value = "worktray", required = false) String worktray) {
        if (!instance.canDelete()) throw new IllegalStateException("can't delete instance in state " + instance.getState().getName());
        instanceService.delete(instance, userService.getCurrentUser());
        if (nextInstance != null) {
            return "redirect:/instances/" + nextInstance + "/process?worktray=" + worktray;
        }
        return "redirect:/titles/" + instance.getTitle().getId();
    }

    @PostMapping("/instances/delete")
    public String deleteSelected(@RequestParam("instance") List<Instance> instances) {
        var user = userService.getCurrentUser();
        for (var instance: instances) {
            instanceService.delete(instance, userService.getCurrentUser());
        }
        return "redirect:" + Requests.backlinkOrDefault("/instances");
    }

    @PostMapping("/instances/{id}/archive")
    @PreAuthorize("hasPermission(#instance.title, 'edit')")
    public String archive(@PathVariable("id") Instance instance,
                          @RequestParam(value = "nextInstance", required = false) Long nextInstance,
                          @RequestParam(value = "worktray", required = false) String worktray) {
        instanceService.archive(instance, userService.getCurrentUser());
        if (nextInstance != null) {
            return "redirect:/instances/" + nextInstance + "/process?worktray=" + worktray;
        }
        return "redirect:/instances/" + instance.getId();
    }

    @PostMapping("/instances/archive")
    public String archiveSelected(@RequestParam("instance") List<Instance> instances) {
        var user = userService.getCurrentUser();
        for (var instance: instances) {
            instanceService.archive(instance, user);
        }
        return "redirect:" + Requests.backlinkOrDefault("/instances");
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
    public ResponseEntity<byte[]> getThumbnail(@PathVariable("id") Instance instance,
                                               @RequestParam(name="type", required = false) InstanceThumbnail.Type type) {
        Optional<InstanceThumbnail> thumbnail;
        if (type == null) {
            thumbnail = instanceThumbnailRepository.findByInstanceAndType(instance, REPLAY)
                    .or(() -> instanceThumbnailRepository.findByInstanceAndType(instance, LIVE));
        } else {
            thumbnail = instanceThumbnailRepository.findByInstanceAndType(instance, type);
        }
        if (thumbnail.isPresent()) {
            return ResponseEntity.ok()
                    .cacheControl(maxAge(1, DAYS))
                    .header("Content-Type", thumbnail.get().getContentType())
                    .body(thumbnail.get().getData());
        } else if (instance.getState().isDeleted()) {
            // icons from material.io/icons
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
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/instances/{id}/log")
    public String log(@PathVariable("id") Instance instance, Model model) throws IOException, ParseException {
        model.addAttribute("instance", instance);
        return "InstanceFiles";
    }

    @GetMapping("/instances/{id}/files")
    public String files(@PathVariable("id") Instance instance,
                        @RequestParam(name = "q", defaultValue = "") String q,
                        @RequestParam MultiValueMap<String, String> params,
                        Model model) throws IOException, ParseException {
        Path indexDir = Paths.get("data/filedex/" + instance.getId());
        var index = new FileSeacher(indexDir);
        FileSeacher.Results results;
        try {
            results = index.search(q, params);
        } catch (IndexNotFoundException e) {
            index.indexRecursively(config.getWorkingDir(instance));
            results = index.search(q, params);
        }

        model.addAttribute("instance", instance);
        model.addAttribute("results", results);
        model.addAttribute("facets", results.facets());
        return "InstanceFiles";
    }

    public record Hit(String url) {
    }

    @GetMapping(value = "/instances/{id}/crawl.log", produces = "text/plain")
    @ResponseBody
    public FileSystemResource crawlLog(@PathVariable("id") Instance instance) {
        return new FileSystemResource(getCrawlLog(instance));
    }

    @NotNull
    private Path getCrawlLog(Instance instance) {
        return Paths.get("data/working/" + instance.getPi() + "/" +
                instance.getDateString() + "/nla.arc-" + instance.getPi() + "-" +
                instance.getDateString() + "/latest/logs/crawl.log");
    }

}
