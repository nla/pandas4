package pandas.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@Controller
@ConditionalOnProperty("OIDC_URL")
public class LoginController {
    private final ClientRegistrationRepository clientRegistrationRepository;

    public LoginController(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @GetMapping("/login")
    public String login(Model model) {
        var registrations = new ArrayList<ClientRegistration>();
        if (!(clientRegistrationRepository instanceof Iterable)) {
            throw new IllegalStateException("clientRegistrationRepository " +
                                               (clientRegistrationRepository.getClass()) +
                                               " is not iterable");
        }
        var iterable = (Iterable<ClientRegistration>) clientRegistrationRepository;
        iterable.forEach(registrations::add);
        Collections.sort(registrations, Comparator.comparing(ClientRegistration::getRegistrationId));
        model.addAttribute("registrations", registrations);
        return "Login";
    }

    @GetMapping(value = "/login/check-session-reply")
    @ResponseBody
    public String checkSessionReply() {
        return "";
    }
}
