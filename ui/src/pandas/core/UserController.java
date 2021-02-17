package pandas.core;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UserController {
    @GetMapping("/users/{id}")
    public String get(@PathVariable("id") long id) {
        return "redirect:/titles?owner=" + id;
    }
}
