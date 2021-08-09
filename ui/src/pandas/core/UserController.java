package pandas.core;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pandas.collection.TitleRepository;

@Controller
public class UserController {
    private final IndividualRepository individualRepository;
    private final TitleRepository titleRepository;

    public UserController(IndividualRepository individualRepository, TitleRepository titleRepository) {
        this.individualRepository = individualRepository;
        this.titleRepository = titleRepository;
    }

    @GetMapping("/users/{userid}")
    public String view(@PathVariable("userid") String userid, Model model) {
        Individual user = individualRepository.findByUserid(userid).orElseThrow(NotFoundException::new);
        model.addAttribute("user", user);
        model.addAttribute("titles", titleRepository.findFirst20ByOwnerOrderByRegDateDesc(user));
        model.addAttribute("titleCount", titleRepository.countByOwner(user));
        return "UserView";
    }
}
