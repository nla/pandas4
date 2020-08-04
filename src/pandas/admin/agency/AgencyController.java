package pandas.admin.agency;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AgencyController {
    private final AgencyRepository agencyRepository;

    public AgencyController(AgencyRepository agencyRepository) {
        this.agencyRepository = agencyRepository;
    }

    @GetMapping("agencies")
    @ResponseBody
    public Iterable<Agency> list() {
        return agencyRepository.findAll();
    }
}
