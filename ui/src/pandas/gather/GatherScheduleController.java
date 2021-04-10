package pandas.gather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pandas.collection.TitleSearcher;
import pandas.core.NotFoundException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Controller
public class GatherScheduleController {
    private static final Logger log = LoggerFactory.getLogger(GatherScheduleController.class);

    private final GatherScheduleRepository gatherScheduleRepository;
    private final TitleGatherRepository titleGatherRepository;
    private final GatherService gatherService;
    private final TitleSearcher titleSearcher;

    public GatherScheduleController(GatherScheduleRepository gatherScheduleRepository, TitleGatherRepository titleGatherRepository, GatherService gatherService, TitleSearcher titleSearcher) {
        this.gatherScheduleRepository = gatherScheduleRepository;
        this.titleGatherRepository = titleGatherRepository;
        this.gatherService = gatherService;
        this.titleSearcher = titleSearcher;
    }

    @GetMapping("/schedules")
    public String list(Model model) {
        model.addAttribute("schedules", gatherService.allGatherSchedules());
        try {
            model.addAttribute("titleCounts", titleSearcher.countTitlesBySchedule());
        } catch (Exception e) {
            log.warn("Title count search failed", e);
            model.addAttribute("titleCounts", Collections.emptyMap());
        }
        return "gather/GatherScheduleList";
    }

    @GetMapping("/schedules/{id}")
    public String get(@PathVariable("id") long id, Model model) {
        GatherSchedule schedule = gatherScheduleRepository.findById(id).orElseThrow(NotFoundException::new);
        model.addAttribute("schedule", schedule);
        return "gather/GatherScheduleEdit";
    }

    @GetMapping("/schedules/new")
    public String newForm(Model model) {
        model.addAttribute("schedule", new GatherSchedule());
        return "gather/GatherScheduleEdit";
    }

    @PostMapping("/schedules/new")
    public String create(GatherSchedule schedule) {
        gatherScheduleRepository.save(schedule);
        return "redirect:/schedules";
    }

    @PostMapping("/schedules/{id}")
    public String update(GatherSchedule schedule) {
        gatherScheduleRepository.save(schedule);
        return "redirect:/schedules";
    }

    @PostMapping("/schedules/{id}/delete")
    public String delete(@PathVariable("id") long id, RedirectAttributes redirAttrs) {
        GatherSchedule schedule = gatherScheduleRepository.findById(id).orElseThrow(NotFoundException::new);
        if (titleGatherRepository.existsBySchedule(schedule)) {
            redirAttrs.addFlashAttribute("danger", "Could not delete schedule '" + schedule.getName() + "' as it is in use by one or more titles.");
        } else {
            gatherScheduleRepository.deleteById(id);
        }
        return "redirect:/schedules";
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E dd MMM uuuu hh:mm a");

    @GetMapping(value = "/schedules/preview", produces = "text/plain")
    @ResponseBody
    public String preview(GatherSchedule schedule) {
        StringBuilder sb = new StringBuilder();
        ZonedDateTime t = ZonedDateTime.now();
        for (int i = 0; i < 15; i++) {
            t = schedule.calculateNextTime(t);
            sb.append(t.format(formatter)).append("\n");
        }
        sb.append("...\n");
        return sb.toString();
    }
}
