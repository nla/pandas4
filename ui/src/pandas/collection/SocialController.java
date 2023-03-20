package pandas.collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.atomic.AtomicLong;

@Controller
public class SocialController {
    private final Logger log = LoggerFactory.getLogger(SocialController.class);
    private final SocialTargetRepository socialTargetRepository;
    private final TitleRepository titleRepository;

    public SocialController(SocialTargetRepository socialTargetRepository, TitleRepository titleRepository) {
        this.socialTargetRepository = socialTargetRepository;
        this.titleRepository = titleRepository;
    }

    @GetMapping("/social")
    public String targetList(Model model) {
        model.addAttribute("targets", socialTargetRepository.findAll());
        return "SocialTargetList";
    }

    @GetMapping("/social/sync")
    @ResponseBody
    @PreAuthorize("hasAuthority('PRIV_SYSADMIN')")
    @Transactional
    public String sync() {
        String server = "twitter.com";
        String nitterBaseUrl = "https://nitter.archive.org.au/";
        AtomicLong added = new AtomicLong();
        titleRepository.findBySeedUrlLike(nitterBaseUrl + "%").forEach(title -> {
            String accountName = title.getSeedUrl().substring(nitterBaseUrl.length());
            accountName = accountName.replaceFirst("[#?/].*", "");
            String query = "from:" + accountName;
            log.info("sync {}", query);
            if (socialTargetRepository.findByTitle(title).isEmpty() &&
                socialTargetRepository.findByServerAndQuery(server, query).isEmpty()) {
                SocialTarget target = new SocialTarget(server, query, title);
                socialTargetRepository.save(target);
                added.incrementAndGet();
            }
        });
        return "Added " + added.get() + " targets";
    }

}
