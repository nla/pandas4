package pandas.agency;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pandas.collection.TitleRepository;

import jakarta.validation.Valid;
import java.io.IOException;

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

    @GetMapping("/agencies/{agencyId}")
    public String view(@PathVariable("agencyId") Agency agency, Model model) {
        model.addAttribute("agency", agency);
        model.addAttribute("users", userRepository.findUsersByAgency(agency));
        model.addAttribute("titleCount", titleRepository.countByAgency(agency));
        model.addAttribute("titles", titleRepository.findFirst20ByAgencyOrderByRegDateDesc(agency));
        return "AgencyView";
    }

    @GetMapping("/agencies/{agencyId}/logo")
    public ResponseEntity<byte[]> logo(@PathVariable("agencyId") Agency agency) {
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

    @GetMapping("/agencies/new")
    @PreAuthorize("hasAuthority('PRIV_EDIT_ALL_AGENCIES')")
    public String newForm(Model model) {
        return edit(new Agency(), model);
    }

    @GetMapping("/agencies/{agencyId}/edit")
    @PreAuthorize("hasPermission(#agency, 'edit')")
    public String edit(@PathVariable("agencyId") Agency agency, Model model) {
        model.addAttribute("agency", agency);
        model.addAttribute("form", AgencyEditForm.of(agency));
        return "AgencyEdit";
    }

    @PostMapping("/agencies/{agencyId}/edit")
    @PreAuthorize("hasPermission(#agency, 'edit')")
    public String update(@PathVariable("agencyId") Agency agency, @Valid AgencyEditForm form) throws IOException {
        form.applyTo(agency);
        agencyRepository.save(agency);
        return "redirect:/agencies/" + agency.getId();
    }

    @PostMapping("/agencies/new")
    @PreAuthorize("hasAuthority('PRIV_EDIT_ALL_AGENCIES')")
    public String create(@Valid AgencyEditForm form) throws IOException {
        Agency agency = new Agency();
        form.applyTo(agency);
        agency = agencyRepository.save(agency);
        agency.getOrganisation().setAgency(agency);
        agencyRepository.save(agency);
        return "redirect:/agencies/" + agency.getId();
    }

    @PostMapping("/agencies/{agencyId}/delete")
    @PreAuthorize("hasPermission(#agency, 'edit')")
    public String delete(@PathVariable("agencyId") Agency agency) throws IOException {
        agencyRepository.delete(agency);
        return "redirect:/agencies";
    }
}
