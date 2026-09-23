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
import java.util.ArrayList;
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
        CollectionScope scope = requireOpenCollectionScope(collectionIds, parent);
        NominationForm form = new NominationForm();
        if (!scope.requiresSelection()) {
            form.setCollectionId(scope.choices().get(0).collection().getId());
        }
        addCollectionsToModel(model, scope);
        model.addAttribute("form", form);
        return "NominationForm";
    }

    @PostMapping("/nominate")
    @PreAuthorize("hasPermission(null, 'Title', 'edit')")
    public String submit(@RequestParam(required = false, name = "collection") List<Long> collectionIds,
                         @RequestParam(required = false) Long parent,
                         @Valid @ModelAttribute("form") NominationForm form,
                         BindingResult bindingResult,
                         Model model) {
        CollectionScope scope = requireOpenCollectionScope(collectionIds, parent);
        addCollectionsToModel(model, scope);

        Collection selectedCollection = null;
        if (form.getCollectionId() == null && scope.roots().size() == 1) {
            selectedCollection = scope.roots().get(0);
            form.setCollectionId(selectedCollection.getId());
        } else if (form.getCollectionId() == null) {
            bindingResult.rejectValue("collectionId", "required", "Select a collection");
        } else {
            selectedCollection = scope.choices().stream()
                    .map(CollectionChoice::collection)
                    .filter(collection -> collection.getId().equals(form.getCollectionId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Selected collection is not available through this nomination link"));
        }

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

        Title title = titleService.nominate(new LinkedHashSet<>(List.of(selectedCollection)), form.getSeedUrl(), form.getName(), form.getContext(),
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

    private CollectionScope requireOpenCollectionScope(List<Long> collectionIds, Long parentId) {
        if ((collectionIds == null || collectionIds.isEmpty()) && parentId != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Parent collection nomination links are not implemented yet");
        }
        if (collectionIds == null || collectionIds.isEmpty() || parentId != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Specify at least one collection and no parent collection");
        }

        Set<Long> uniqueIds = new LinkedHashSet<>(collectionIds);
        List<Collection> roots = uniqueIds.stream().map(collectionId ->
                collectionRepository.findById(collectionId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Collection not found: " + collectionId))).toList();
        if (roots.stream().anyMatch(Collection::isAncestorClosed)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A collection is closed to new additions");
        }

        List<Collection> availableCollections = new ArrayList<>();
        Set<Long> visited = new LinkedHashSet<>();
        for (Collection root : roots) {
            addOpenDescendants(root, availableCollections, visited);
        }

        List<CollectionChoice> choices;
        if (roots.size() == 1 && availableCollections.size() > 1) {
            String parentPrefix = roots.get(0).getFullName() + "—";
            choices = availableCollections.subList(1, availableCollections.size()).stream()
                    .map(collection -> new CollectionChoice(collection,
                            collection.getFullName().substring(parentPrefix.length())))
                    .toList();
        } else {
            choices = availableCollections.stream()
                    .map(collection -> new CollectionChoice(collection, collection.getFullName()))
                    .toList();
        }
        return new CollectionScope(roots, choices, roots.size() > 1 || availableCollections.size() > 1);
    }

    private void addOpenDescendants(Collection collection, List<Collection> options, Set<Long> visited) {
        if (collection.isAncestorClosed() || !visited.add(collection.getId())) {
            return;
        }
        options.add(collection);
        for (Collection child : collectionRepository.findByParentOrderByName(collection)) {
            addOpenDescendants(child, options, visited);
        }
    }

    private void addCollectionsToModel(Model model, CollectionScope scope) {
        List<Collection> collections = scope.roots();
        model.addAttribute("collections", collections);
        model.addAttribute("collectionChoices", scope.choices());
        model.addAttribute("collectionSelectionRequired", scope.requiresSelection());
        model.addAttribute("collectionNames",
                String.join(", ", collections.stream().map(Collection::getFullName).toList()));
        model.addAttribute("primaryCollection", collections.get(0));
    }

    private record CollectionChoice(Collection collection, String label) {}

    private record CollectionScope(List<Collection> roots, List<CollectionChoice> choices,
                                   boolean requiresSelection) {}
}
