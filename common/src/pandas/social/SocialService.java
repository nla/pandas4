package pandas.social;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pandas.collection.SocialTarget;
import pandas.collection.SocialTargetRepository;
import pandas.collection.TitleRepository;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class SocialService {
    private final Logger log = LoggerFactory.getLogger(SocialService.class);
    private final SocialTargetRepository socialTargetRepository;
    private final TitleRepository titleRepository;

    public SocialService(SocialTargetRepository socialTargetRepository, TitleRepository titleRepository) {
        this.socialTargetRepository = socialTargetRepository;
        this.titleRepository = titleRepository;
    }

    @Transactional
    public String syncTitlesToSocialTargets() {
        log.info("Syncing titles to social targets");
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
            log.trace("Syncing {}", query);
            if (socialTargetRepository.findByTitle(title).isEmpty() &&
                    socialTargetRepository.findByServerAndQueryIgnoreCase(server, query).isEmpty()) {
                SocialTarget target = new SocialTarget(server, query, title);
                socialTargetRepository.save(target);
                added.incrementAndGet();
            }
        });
        log.info("Added {} social targets", added.get());
        return "Added " + added.get() + " targets";
    }
}
