package pandas.discovery;

import info.freelibrary.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.search.mapper.orm.Search;
import org.netpreserve.urlcanon.Canonicalizer;
import org.netpreserve.urlcanon.ParsedUrl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pandas.collection.Title;
import pandas.search.SearchResults;

import java.io.IOException;

import static org.hibernate.search.engine.search.common.BooleanOperator.AND;

@Controller
public class DiscoveryController {
    private final DiscoveryRepository discoveryRepository;
    private final EntityManager entityManager;

    public DiscoveryController(DiscoveryRepository discoveryRepository, EntityManager entityManager) {
        this.discoveryRepository = discoveryRepository;
        this.entityManager = entityManager;
    }

    @GetMapping("/discoveries")
    public String index(@PageableDefault(size = 100) Pageable pageable,
                        @RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "false") boolean showDotAu,
                        @RequestParam(defaultValue = "false") boolean showAlreadySelected,
                        Model model) {
        String cleanQ = StringUtils.trimToNull(q);
        var search = Search.session(entityManager).search(Discovery.class)
                .where((f, root) -> {
                    if (!showDotAu) root.add(f.match().field("dotAu").matching(false));
                    if (!showAlreadySelected) root.add(f.match().field("alreadySelected").matching(false));
                    if (cleanQ != null) root.add(f.simpleQueryString().field("name").matching(cleanQ).defaultOperator(AND));
                    if (!root.hasClause()) root.add(f.matchAll());
                }).sort(s -> cleanQ == null ? s.field("createdDate").desc() : s.score());

        var result = search.fetch((int) pageable.getOffset(), pageable.getPageSize());
        SearchResults<Discovery> results = new SearchResults<>(result, null, pageable);
        model.addAttribute("q", cleanQ);
        model.addAttribute("showDotAu", showDotAu);
        model.addAttribute("showAlreadySelected", showAlreadySelected);
        model.addAttribute("discoveries", results);
        return "discovery/DiscoverySearch";
    }

    @GetMapping("/discoveries/reindex")
    @ResponseBody
    @PreAuthorize("hasAuthority('PRIV_SYSADMIN')")
    public String reindex() throws InterruptedException {
        Search.session(entityManager).massIndexer(Discovery.class).startAndWait();
        return "OK";
    }

    @GetMapping(value = "/discoveries/associate", produces = "text/plain")
    @ResponseBody
    @PreAuthorize("hasAuthority('PRIV_SYSADMIN')")
    public void associate(HttpServletResponse response) throws IOException {
        var writer = response.getWriter();
        long lastId = Long.MIN_VALUE;
        while (true) {
            var discoveries = discoveryRepository.findByTitleIsNullAndIdGreaterThanOrderById(lastId, PageRequest.of(0, 100));
            if (discoveries.isEmpty()) break;
            outer: for (Discovery discovery : discoveries) {
                lastId = discovery.getId();
                var hits = Search.session(entityManager).search(Title.class)
                        .where(f -> f.phrase().field("titleUrl").matching(discovery.getUrl()))
                        .fetchHits(100);
                if (hits.isEmpty()) {
                    writer.println("No hits for " + discovery.getId() + " " + discovery.getName() + " " + discovery.getUrl());
                    continue;
                }
                String discoveryCanonUrl = canonicalizeUrl(discovery.getUrl());
                Title matchingTitle = null;
                for (Title title : hits) {
                    String titleCanonUrl = canonicalizeUrl(title.getTitleUrl());
                    if (discoveryCanonUrl.equals(titleCanonUrl)) {
                        matchingTitle = title;
                        break;
                    }
                }

                if (matchingTitle != null) {
                    writer.println("Exact match for " + discovery.getId() + " " + discovery.getName() + " " + discovery.getUrl());
                    writer.println(" - " + matchingTitle.getPi() + " " + matchingTitle.getName() + " " + matchingTitle.getTitleUrl());
                    discovery.setTitle(matchingTitle);
                    discoveryRepository.save(discovery);
                } else {
                    writer.println("Hits but not match for " + discovery.getId() + " " + discovery.getName() + " " + discovery.getUrl());
                }
            }
        }
    }

    private String canonicalizeUrl(String url) {
        ParsedUrl parsedUrl = ParsedUrl.parseUrl(url);
        Canonicalizer.AGGRESSIVE.canonicalize(parsedUrl);
        return parsedUrl.toString();
    }
}
