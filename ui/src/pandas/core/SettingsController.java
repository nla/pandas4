package pandas.core;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsController {
    @GetMapping("/settings")
    public String systemSettings() {
        return "SystemSettings";
    }

    @GetMapping("/pandas3-eol")
    public String pandas3EndOfLife() {
        return "Pandas3Eol";
    }
}
