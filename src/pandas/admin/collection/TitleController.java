package pandas.admin.collection;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;
import pandas.admin.Config;

@Controller
public class TitleController {
    private final TitleRepository titleRepository;
    private final Config config;

    public TitleController(TitleRepository titleRepository, Config config) {
        this.titleRepository = titleRepository;
        this.config = config;
    }

    @GetMapping("/titles/{id}")
    public RedirectView get(@PathVariable("id") long id) {
        return new RedirectView(config.managementDirectActionUrl("titleView?id=" + id));
    }
}
