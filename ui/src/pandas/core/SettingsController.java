package pandas.core;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsController {
    @GetMapping("/settings")
    public String systemSettings() {
         return "SystemSettings";
     }
}
