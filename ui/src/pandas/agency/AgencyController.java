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

    @GetMapping("/agencies/{id}")
    public String view(@PathVariable("id") Agency agency, Model model) {
        model.addAttribute("agency", agency);
        model.addAttribute("users", individualRepository.findUsersByAgency(agency));
        model.addAttribute("titleCount", titleRepository.countByAgency(agency));
        model.addAttribute("titles", titleRepository.findFirst20ByAgencyOrderByRegDateDesc(agency));
        return "AgencyView";
    }

    @GetMapping("/agencies/{id}/logo")
    public ResponseEntity<byte[]> logo(@PathVariable("id") Agency agency) {
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
