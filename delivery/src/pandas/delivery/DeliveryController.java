package pandas.delivery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.collection.*;
import pandas.delivery.util.CountingSet;
import pandas.util.ServletUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

@Controller
@RequestMapping({"/", "/partner/{partner}"})
public class DeliveryController {
    private static final List<String> ALPHABET = List.of("1-9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
            "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");

    private final Logger log = LoggerFactory.getLogger(DeliveryController.class);
    private final SubjectRepository subjectRepository;
    private final TitleRepository titleRepository;
    private final AgencyRepository agencyRepository;
    private final CollectionRepository collectionRepository;

    public DeliveryController(SubjectRepository subjectRepository, TitleRepository titleRepository, AgencyRepository agencyRepository, CollectionRepository collectionRepository) {
        this.subjectRepository = subjectRepository;
        this.titleRepository = titleRepository;
        this.agencyRepository = agencyRepository;
        this.collectionRepository = collectionRepository;
    }

    @ModelAttribute
    public void attributes(@PathVariable(value = "partner", required = false) String partner, Model model) {
        model.addAttribute("allSubjects", subjectRepository.findAllByOrderByName());
        model.addAttribute("agencyFilter", partner == null ? null : agencyRepository.findByAlias(partner).orElseThrow());
        model.addAttribute("prefix", partner == null ? "" : "/partner/" + partner);
    }

    @GetMapping({"/", ""})
    public String home(Model model) {
        model.addAttribute("topLevelSubjects", subjectRepository.findByParentIsNullOrderByName());
        model.addAttribute("alphabet", ALPHABET);
        model.addAttribute("frontpageData", getFrontpageData());
        return "Home";
    }

    @GetMapping({"/subject/{id}", "/subject/{id}/{page}"})
    public String subject(@ModelAttribute("agencyFilter") Agency agencyFilter, @PathVariable("id") Subject subject,
                          @PathVariable("page") Optional<Integer> page, Model model) {
        var pageable = PageRequest.of(page.orElse(1) - 1, 100);
        model.addAttribute("subject", subject);
        model.addAttribute("collections", collectionRepository.findByParentIsNullAndSubjectsContainsOrderByName(subject));
        Page<TitleBrief> titles = titleRepository.findPublishedTitlesInSubject(subject, agencyFilter, pageable);
        model.addAttribute("titles", titles);
        var agencies = new CountingSet<Agency>();
        titles.forEach(title -> agencies.add(title.getAgency()));
        model.addAttribute("agencies", agencies.listByFrequencyDecreasing());
        return "Subject";
    }

    @GetMapping("/subject/{id}/icon")
    @Transactional
    public void subjectIcon(@PathVariable("id") Subject subject, HttpServletResponse response) throws IOException, SQLException {
        if (subject.getIcon() == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        ServletUtils.sendBlobAsImage(subject.getIcon(), response);
    }

    @GetMapping({"/alpha/{letter}", "/alpha/{letter}/{page}"})
    public String alpha(@PathVariable("letter") String letter,
                        @PathVariable("page") Optional<Integer> page, Model model) {
        var pageable = PageRequest.of(page.orElse(1) - 1, 100);
        Page<Title> titles;
        if (letter.equals("1-9")) {
            titles = titleRepository.findDisplayableTitlesWithNumberNames(pageable);
            model.addAttribute("collections", collectionRepository.findTopLevelDisplayableCollectionsWithNumberNames(pageable));
        } else {
            if ("ALL".equals(letter)) letter = "%";
            else if (letter.contains("%")) throw new IllegalArgumentException();
            else if (letter.length() != 1) throw new IllegalArgumentException();
            titles = titleRepository.findDisplayableTitlesNamedLike(letter + "%", pageable);
            model.addAttribute("collections", collectionRepository.findTopLevelDisplayableCollectionsNamedLike(letter + "%", pageable));
        }
        var agencies = new CountingSet<Agency>();
        titles.forEach(title -> agencies.add(title.getAgency()));
        model.addAttribute("agencies", agencies.listByFrequencyDecreasing());
        model.addAttribute("titles", titles);
        model.addAttribute("alphabet", ALPHABET);
        return "Alpha";
    }

    @GetMapping({"/col/{id}", "/col/{id}/{page}"})
    public String collection(@PathVariable("id") Collection collection,
                          @PathVariable("page") Optional<Integer> page, Model model) {
        model.addAttribute("collection", collection);


        List<Title> titles = titleRepository.findPublishedTitlesInCollection(collection);
        var agencies = new CountingSet<Agency>();
        titles.forEach(t -> agencies.add(t.getAgency()));
        model.addAttribute("titles", titles);

        var titlesOfChildren = new HashMap<Long, List<Title>>();
        for (var child: collection.getChildren()) {
            List<Title> childTitles = titleRepository.findPublishedTitlesInCollection(child);
            titlesOfChildren.put(child.getId(), childTitles);
            childTitles.forEach(t -> agencies.add(t.getAgency()));
        }

        model.addAttribute("titlesOfChildren", titlesOfChildren);
        model.addAttribute("agencies", agencies.listByFrequencyDecreasing());
        return "Collection";
    }

    @GetMapping(value = "/agency/{alias}/logo", produces = "image/gif")
    @ResponseBody
    public ResponseEntity<byte[]> agencyLogo(@PathVariable String alias) {
        var agency = agencyRepository.findByAlias(alias).orElseThrow();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .body(agency.getLogo());
    }

    private String getFrontpageData() {
        String url = "https://pandora.nla.gov.au/frontpage.html";
        String frontpageData;
        try {
            frontpageData = new String(new URL(url).openStream().readAllBytes(), UTF_8);
        } catch (IOException e) {
            log.warn("Unable to fetch " + url, e);
            frontpageData = "";
        }
        return frontpageData;
    }
}
