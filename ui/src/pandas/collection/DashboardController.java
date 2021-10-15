package pandas.collection;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pandas.core.UserService;

@Controller
public class DashboardController {
    private final TitleRepository titleRepository;
    private final UserService userService;

    public DashboardController(TitleRepository titleRepository, UserService userService) {
        this.titleRepository = titleRepository;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("nominations", titleRepository.findByNominator(userService.getCurrentUser(), 
                Pageable.ofSize(20)));
        return "Dashboard";
    }

}
