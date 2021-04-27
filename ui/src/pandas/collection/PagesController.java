package pandas.collection;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pandas.util.DateFormats;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class PagesController {
    private final CaptureIndex captureIndex;

    public PagesController(CaptureIndex captureIndex) {
        this.captureIndex = captureIndex;
    }

    @GetMapping("/pages")
    public String search(@RequestParam(value = "url", required = false) String url, Model model) {
        List<Capture> captures = url == null || url.isBlank() ? List.of() : captureIndex.query(url);
        model.addAttribute("captures", captures);
        model.addAttribute("url", url);
        model.addAttribute("dateTimeFormat", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault()));
        return "PageSearch";
    }

    @GetMapping("/replay")
    public String replay(@RequestParam String url, @RequestParam Instant date, Model model) {
        model.addAttribute("url", url);
        model.addAttribute("arcdate", DateFormats.ARC_DATE.format(date));
        return "PageView";
    }
}
