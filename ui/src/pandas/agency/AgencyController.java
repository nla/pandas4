package pandas.agency;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import pandas.collection.TitleRepository;
import pandas.core.IndividualRepository;
import pandas.core.NotFoundException;

@Controller
public class AgencyController {
    private final AgencyRepository agencyRepository;
    private final IndividualRepository individualRepository;
    private final TitleRepository titleRepository;

    public AgencyController(AgencyRepository agencyRepository, IndividualRepository individualRepository, TitleRepository titleRepository) {
        this.agencyRepository = agencyRepository;
        this.individualRepository = individualRepository;
        this.titleRepository = titleRepository;
    }

    @GetMapping("agencies")
    @ResponseBody
    public Iterable<Agency> list() {
        return agencyRepository.findAll();
    }

    @GetMapping("/agencies/{alias}")
    public String view(@PathVariable("alias") String alias, Model model) {
        Agency agency = agencyRepository.findByAlias(alias).orElseThrow(NotFoundException::new);
        model.addAttribute("agency", agency);
        model.addAttribute("users", individualRepository.findUsersByAgency(agency));
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
}
