package pandas.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

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

    @GetMapping(value = "/update-password")
    public String updatePassword(Authentication authentication) {
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        ClientRegistration clientRegistration = this.clientRegistrationRepository
                .findByRegistrationId(registrationId);
        String authzEndpoint = clientRegistration.getProviderDetails().getAuthorizationUri();
        String callbackUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/update-password-callback").toUriString();
        String actionUrl = UriComponentsBuilder.fromUriString(authzEndpoint)
                .queryParam(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId())
                .queryParam(OAuth2ParameterNames.RESPONSE_TYPE, OAuth2AuthorizationResponseType.CODE.getValue())
                .queryParam(OAuth2ParameterNames.SCOPE, clientRegistration.getScopes())
                .queryParam(OAuth2ParameterNames.REDIRECT_URI, callbackUrl)
                .queryParam("kc_action", "UPDATE_PASSWORD")
                .toUriString();
        return "redirect:" + actionUrl;
    }

    @GetMapping(value = "/update-password-callback")
    public String updatePasswordCallback() {
        return "redirect:/";
    }
}
