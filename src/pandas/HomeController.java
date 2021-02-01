package pandas;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "redirect:/titles";
    }

    @GetMapping("/whoami")
    @ResponseBody
    public Principal whoami(Principal principal) {
        return principal;
    }
}
