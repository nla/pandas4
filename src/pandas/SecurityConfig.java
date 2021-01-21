package pandas;

import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        String issuerUrl = System.getProperty("spring.security.oauth2.client.provider.oidc.issuer-uri");
        if (issuerUrl != null) {
            http.oauth2Login().userInfoEndpoint().oidcUserService(oidcUserService());
            http.logout().logoutSuccessUrl("/");
            http.authorizeRequests()
                    .antMatchers("/actuator/health").anonymous()
                    .antMatchers("/collections", "/collections/**").hasRole("panadmin")
                    .antMatchers("/gather/**").hasRole("panadmin")
                    .antMatchers("/schedules", "/schedules/**").hasRole("panadmin")
                    .anyRequest().hasRole("stduser");
        }
    }

    private Set<GrantedAuthority> mapClaimsToAuthorities(Map<String, Object> claims) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        JSONArray array = (JSONArray) (((JSONObject) claims.get("realm_access")).get("roles"));
        for (var entry : array) {
            if (entry instanceof String) {
                var role = (String) entry;
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }
        return authorities;
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcUserService delegate = new OidcUserService();
        return (userRequest) -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);
            String accessTokenValue = userRequest.getAccessToken().getTokenValue();
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>(oidcUser.getAuthorities());
            try {
                JSONObject decoded = SignedJWT.parse(accessTokenValue).getPayload().toJSONObject();
                mappedAuthorities.addAll(mapClaimsToAuthorities(decoded));
            } catch (ParseException e) {
                log.error("Error parsing access token", e);
            }
            String usernameAttribute = userRequest.getClientRegistration().getProviderDetails()
                    .getUserInfoEndpoint().getUserNameAttributeName();
            return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), usernameAttribute);
        };
    }
}

