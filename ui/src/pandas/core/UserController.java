package pandas.core;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pandas.collection.TitleRepository;

@Controller
public class UserController {
    private final TitleRepository titleRepository;

    public UserController(TitleRepository titleRepository) {
        this.titleRepository = titleRepository;
    }

    @GetMapping("/users/{id}")
    public String view(@PathVariable("id") Individual user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("titles", titleRepository.findFirst20ByOwnerOrderByRegDateDesc(user));
        model.addAttribute("titleCount", titleRepository.countByOwner(user));
        return "UserView";
    }
}
