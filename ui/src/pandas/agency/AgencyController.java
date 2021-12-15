package pandas.agency;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pandas.collection.TitleRepository;
import pandas.core.NotFoundException;

@Controller
public class AgencyController {
    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final TitleRepository titleRepository;

    public AgencyController(AgencyRepository agencyRepository, UserRepository userRepository, TitleRepository titleRepository) {
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
        this.titleRepository = titleRepository;
    }

    @GetMapping("/agencies")
    public String list(Model model) {
        model.addAttribute("agencies", agencyRepository.summarizeAllOrdered());
        return "AgencyList";
    }

    @GetMapping("/agencies/{alias}")
    public String view(@PathVariable("alias") String alias, Model model) {
        Agency agency = agencyRepository.findByAlias(alias).orElseThrow(NotFoundException::new);
        model.addAttribute("agency", agency);
        model.addAttribute("users", userRepository.findUsersByAgency(agency));
        model.addAttribute("titleCount", titleRepository.countByAgency(agency));
        model.addAttribute("titles", titleRepository.findFirst20ByAgencyOrderByRegDateDesc(agency));
        return "AgencyView";
    }

    @GetMapping("/agencies/{alias}/logo")
    public ResponseEntity<byte[]> logo(@PathVariable("alias") String alias) {
        Agency agency = agencyRepository.findByAlias(alias).orElseThrow(NotFoundException::new);
        byte[] logo = agency.getLogo();
        MediaType type = MediaType.APPLICATION_OCTET_STREAM;
        if (logo.length > 0) {
            if (logo[0] == (byte)0x89) {
                type = MediaType.IMAGE_PNG;
            } else if (logo[0] == 'G') {
                type = MediaType.IMAGE_GIF;
            } else if (logo[0] == (byte)0xFF) {
                type = MediaType.IMAGE_JPEG;
            }
        }
        return ResponseEntity.ok().contentType(type).body(logo);
    }

    @GetMapping("/agencies/{alias}/edit")
    public String edit(@PathVariable("alias") String alias, Model model) {
        Agency agency = agencyRepository.findByAlias(alias).orElseThrow(NotFoundException::new);
        model.addAttribute("agency", agency);
        model.addAttribute("form", AgencyEditForm.of(agency));
        return "AgencyEdit";
    }
}
