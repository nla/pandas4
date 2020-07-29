package pandas.admin.collection;

import org.hibernate.search.jpa.Search;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import pandas.admin.Config;

import javax.persistence.EntityManager;

@Controller
public class TitleController {
    private final TitleRepository titleRepository;
    private final Config config;
    private final EntityManager entityManager;

    public TitleController(TitleRepository titleRepository, Config config, EntityManager entityManager) {
        this.titleRepository = titleRepository;
        this.config = config;
        this.entityManager = entityManager;
    }

    @GetMapping("/titles/{id}")
    public RedirectView get(@PathVariable("id") long id) {
        return new RedirectView(config.managementDirectActionUrl("titleView?id=" + id));
    }

    @GetMapping("/titles/reindex")
    @ResponseBody
    public String reindex() throws InterruptedException {
        Search.getFullTextEntityManager(entityManager).createIndexer(Title.class).startAndWait();
        return "ok";
    }
}
