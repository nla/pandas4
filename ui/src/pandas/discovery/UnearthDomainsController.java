package pandas.discovery;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pandas.agency.Agency;
import pandas.agency.UserService;
import pandas.collection.Reason;
import pandas.collection.TitleSearcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UnearthDomainsController {
    private final DomainSearcher domainSearcher;
    private final TitleSearcher titleSearcher;
    private final RejectedDomainRepository rejectedDomainRepository;
    private final UserService userService;

    public UnearthDomainsController(DomainSearcher domainSearcher,
                                    TitleSearcher titleSearcher,
                                    RejectedDomainRepository rejectedDomainRepository, UserService userService) {
        this.domainSearcher = domainSearcher;
        this.titleSearcher = titleSearcher;
        this.rejectedDomainRepository = rejectedDomainRepository;
        this.userService = userService;
    }

    @GetMapping("/unearth/domains")
    public String unearth(@RequestParam(required = false) String q,
                          @RequestParam(defaultValue = "false") boolean includeExisting,
                          @RequestParam(defaultValue = "false") boolean includeRejected,
                          Model model) throws IOException {
        model.addAttribute("q", q);
        model.addAttribute("domains", List.of());
        model.addAttribute("includeExisting", includeExisting);
        model.addAttribute("includeRejected", includeRejected);
        if (q != null) {
            var negativeTerms = new ArrayList<String>();
            var normalTerms = new ArrayList<String>();
            var mandatoryTerms = new ArrayList<String>();
            for (String term : q.split("\\s+")) {
                if (term.startsWith("-")) {
                    negativeTerms.add(term.substring(1));
                } else if (term.startsWith("+")) {
                    mandatoryTerms.add(term.substring(1));
                } else {
                    normalTerms.add(term);
                }
            }

            // we need at least one normal term for the initial search
            if (normalTerms.isEmpty() && !mandatoryTerms.isEmpty()) {
                normalTerms.add(mandatoryTerms.get(0));
                mandatoryTerms.remove(0);
            }

            List<String> domains = domainSearcher.searchDomains(normalTerms, domain -> {
                for (String term : negativeTerms) {
                    if (domain.contains(term)) return false;
                }
                for (String term : mandatoryTerms) {
                    if (!domain.contains(term)) return false;
                }
                return true;
            }, 500);
            if (!includeExisting) {
                domains = domains.stream().parallel()
                        .filter(domain -> titleSearcher.urlCheck(domain).isEmpty())
                        .limit(500)
                        .toList();
            }
            var agency = userService.getCurrentUser().getAgency();

            Multimap<String, Integer> rejectedDomains = HashMultimap.create();
            for (RejectedDomain rejectedDomain : rejectedDomainRepository.findRejectedDomains(domains, agency)) {
                rejectedDomains.put(rejectedDomain.getDomain(), (int)(long)rejectedDomain.getReason().getId());
            }
            if (!includeRejected) {
                domains = domains.stream().filter(domain -> !rejectedDomains.containsKey(domain)).toList();
            }

            model.addAttribute("rejectedDomains", rejectedDomains);
            model.addAttribute("domains", domains);
        }
        return "discovery/UnearthDomains";
    }

    @PostMapping("/unearth/domains/reject")
    @ResponseBody
    @Transactional
    public String rejectDomain(@RequestParam String domain,
                               @RequestParam(required = false) Reason reason) {
        var user = userService.getCurrentUser();
        // delete any previous rejections by our own agency, or system-wide rejections
        // but keep any agency-specific rejections by other agencies
        rejectedDomainRepository.deleteByDomainAndAgency(domain, user.getAgency());
        rejectedDomainRepository.deleteByDomainAndAgency(domain, null);
        if (reason != null) {
            Agency agency;
            if (reason.getName().equals("Responsibility of another agency")) {
                agency = user.getAgency();
            } else {
                agency = null; // system-wide rejection
            }
            rejectedDomainRepository.save(new RejectedDomain(domain, reason, agency));
        }
        return "OK";
    }
}
