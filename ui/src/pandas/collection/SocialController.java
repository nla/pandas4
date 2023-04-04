package pandas.collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class SocialController {
    private final Logger log = LoggerFactory.getLogger(SocialController.class);
    private final SocialClient socialClient;
    private final SocialTargetRepository socialTargetRepository;
    private final TitleRepository titleRepository;

    public SocialController(SocialClient socialClient, SocialTargetRepository socialTargetRepository, TitleRepository titleRepository) {
        this.socialClient = socialClient;
        this.socialTargetRepository = socialTargetRepository;
        this.titleRepository = titleRepository;
    }

    @GetMapping("/social")
    public String targetList(Model model) {
        var targets = socialTargetRepository.findAll(Sort.by(Sort.Order.asc("query").ignoreCase()));
        model.addAttribute("targets", targets);
        return "SocialTargetList";
    }

    @GetMapping("/social/search")
    public String search(@RequestParam("q") String q,
                         @RequestParam(value = "sort", defaultValue = "Relevance") String sort,
                         Model model) throws IOException {
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("orderings", List.of("Relevance", "Newest", "Oldest",
                "Likes", "Replies", "Reposts", "Quotes"));
        model.addAttribute("results", socialClient.search(q, sort.toLowerCase()));
        return "SocialSearch";
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
            accountName = accountName.replaceFirst("^@", "");
            if (accountName.isBlank()) {
                log.warn("Blank account name for pi={} url={}", title.getPi(), title.getSeedUrl());
                return;
            }
            if (!title.getStatus().isActive()) {
                return;
            }
            String query = "from:" + accountName;
            log.info("sync {}", query);
            if (socialTargetRepository.findByTitle(title).isEmpty() &&
                socialTargetRepository.findByServerAndQueryIgnoreCase(server, query).isEmpty()) {
                SocialTarget target = new SocialTarget(server, query, title);
                socialTargetRepository.save(target);
                added.incrementAndGet();
            }
        });
        return "Added " + added.get() + " targets";
    }

}
