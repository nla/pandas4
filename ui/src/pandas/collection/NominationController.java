package pandas.collection;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.agency.UserService;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Controller
public class NominationController {
    private static final Logger log = LoggerFactory.getLogger(NominationController.class);

    private final CollectionRepository collectionRepository;
    private final TitleService titleService;
    private final UserService userService;
    private final TitleSearcher titleSearcher;
    private final CaptureIndex captureIndex;

    public NominationController(CollectionRepository collectionRepository, TitleService titleService,
                                UserService userService,
                                TitleSearcher titleSearcher, CaptureIndex captureIndex) {
        this.collectionRepository = collectionRepository;
        this.titleService = titleService;
        this.userService = userService;
        this.titleSearcher = titleSearcher;
        this.captureIndex = captureIndex;
    }

    @GetMapping("/nominate")
    @PreAuthorize("hasPermission(null, 'Title', 'edit')")
    public String form(@RequestParam(required = false, name = "collection") List<Long> collectionIds,
                       @RequestParam(required = false) Long parent,
                       Model model) {
        List<Collection> collections = requireOpenFixedCollections(collectionIds, parent);
        addCollectionsToModel(model, collections);
        model.addAttribute("form", new NominationForm());
        return "NominationForm";
    }

    @PostMapping("/nominate")
    @PreAuthorize("hasPermission(null, 'Title', 'edit')")
    public String submit(@RequestParam(required = false, name = "collection") List<Long> collectionIds,
                         @RequestParam(required = false) Long parent,
                         @Valid @ModelAttribute("form") NominationForm form,
                         BindingResult bindingResult,
                         Model model) {
        List<Collection> collections = requireOpenFixedCollections(collectionIds, parent);
        addCollectionsToModel(model, collections);

        if (!bindingResult.hasFieldErrors("seedUrl")) {
            try {
                form.setSeedUrl(TitleService.normalizeNominationUrl(form.getSeedUrl()));
            } catch (IllegalArgumentException e) {
                bindingResult.rejectValue("seedUrl", "invalid", e.getMessage());
            }
        }

        if (bindingResult.hasErrors()) {
            return "NominationForm";
        }

        Title title = titleService.nominate(new LinkedHashSet<>(collections), form.getSeedUrl(), form.getName(), form.getContext(),
                userService.getCurrentUser());
        return "redirect:/titles/" + title.getId();
    }

    @GetMapping("/nominate/check")
    @PreAuthorize("hasPermission(null, 'Title', 'edit')")
    @ResponseBody
    public NominationUrlCheck checkUrl(@RequestParam String url) {
        String normalizedUrl = TitleService.normalizeNominationUrl(url);
        URI uri = URI.create(normalizedUrl);
        String siteRoot = uri.getScheme() + "://" + uri.getRawAuthority() + "/";
        var matches = titleSearcher.urlCheck(siteRoot);
        if (matches.isEmpty()) {
            return new NominationUrlCheck(false, null);
        }

        LatestSnapshot snapshot = null;
        try {
            snapshot = captureIndex.latestSuccessful(normalizedUrl)
                    .map(capture -> new LatestSnapshot(capture.getDate(), capture.getReplayUrl()))
                    .orElse(null);
        } catch (RuntimeException e) {
            log.warn("Unable to look up the latest archived snapshot for {}", normalizedUrl, e);
        }
        return new NominationUrlCheck(true, snapshot);
    }

    public record NominationUrlCheck(boolean existingTitle, LatestSnapshot latestSnapshot) {}
    public record LatestSnapshot(Instant date, String url) {}

    private List<Collection> requireOpenFixedCollections(List<Long> collectionIds, Long parentId) {
        if ((collectionIds == null || collectionIds.isEmpty()) && parentId != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Parent collection nomination links are not implemented yet");
        }
        if (collectionIds == null || collectionIds.isEmpty() || parentId != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Specify at least one collection and no parent collection");
        }

        Set<Long> uniqueIds = new LinkedHashSet<>(collectionIds);
        List<Collection> collections = uniqueIds.stream().map(collectionId ->
                collectionRepository.findById(collectionId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Collection not found: " + collectionId))).toList();
        if (collections.stream().anyMatch(Collection::isAncestorClosed)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A collection is closed to new additions");
        }
        return collections;
    }

    private void addCollectionsToModel(Model model, List<Collection> collections) {
        model.addAttribute("collections", collections);
        model.addAttribute("collectionNames",
                String.join(", ", collections.stream().map(Collection::getFullName).toList()));
        model.addAttribute("primaryCollection", collections.get(0));
    }
}
