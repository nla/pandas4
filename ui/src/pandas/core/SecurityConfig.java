package pandas.core;

import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri:#{null}}")
    private String oidcIssuerUri;

    private final PandasUserDetailsService pandasUserDetailsService;
    private final Config config;

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final IndividualRepository individualRepository;

    public SecurityConfig(PandasUserDetailsService pandasUserDetailsService, Config config,
                          @Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository, IndividualRepository individualRepository) {
        this.pandasUserDetailsService = pandasUserDetailsService;
        this.config = config;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.individualRepository = individualRepository;
        if (config.getAutologin() != null) {
            log.warn("DANGER! Running with auto-login as user '{}'. This should never be used in production.", config.getAutologin());
        }
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        if (config.getAutologin() != null) {
            PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
            provider.setPreAuthenticatedUserDetailsService(pandasUserDetailsService);
            auth.authenticationProvider(provider);
        }
        if (oidcIssuerUri != null) {
            super.configure(auth);
        } else {
            DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
            authProvider.setUserDetailsService(pandasUserDetailsService);
            authProvider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
            auth.authenticationProvider(authProvider);
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (oidcIssuerUri != null) {
            http.oauth2Login().userInfoEndpoint().oidcUserService(oidcUserService())
                    .and().loginPage("/login")
                    .and().logout().logoutUrl("/logout");
            //http.logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
        }
        if (config.getAutologin() != null) {
            UserDetails user = pandasUserDetailsService.loadUserByUsername(config.getAutologin());
            AbstractPreAuthenticatedProcessingFilter filter = new AbstractPreAuthenticatedProcessingFilter() {
                protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
                    return user;
                }

                protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
                    return "dummy-credentials";
                }
            };
            filter.setAuthenticationManager(authenticationManager());
            http.addFilterBefore(filter, AnonymousAuthenticationFilter.class);
        }
        var auth = http.authorizeRequests()
                .mvcMatchers("/actuator/health").anonymous()
                .mvcMatchers("/titles/check").permitAll()
                .mvcMatchers("/login").permitAll()
                .mvcMatchers("/assets/**").permitAll()
                .anyRequest().hasRole("stduser");
        if (oidcIssuerUri != null && clientRegistrationRepository != null) {
            auth.and().logout().logoutSuccessHandler((request, response, authentication) -> {
                OidcClientInitiatedLogoutSuccessHandler handler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
                handler.setPostLogoutRedirectUri(ServletUriComponentsBuilder.fromContextPath(request).build().toUriString());
                handler.onLogoutSuccess(request, response, authentication);
            });
        }
        if (oidcIssuerUri == null && config.getAutologin() == null) {
                auth.and().formLogin().defaultSuccessUrl("/test")
                    .and().httpBasic().realmName("PANDAS");
        }
    }

    @SuppressWarnings("unchecked")
    private Set<GrantedAuthority> mapClaimsToAuthorities(Map<String, Object> claims) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (String role : (List<String>)((Map<String,Object>)claims.get("realm_access")).get("roles")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            authorities.addAll(Privileges.byRole.getOrDefault(role, Set.of()));
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
                mappedAuthorities.addAll(mapClaimsToAuthorities(SignedJWT.parse(accessTokenValue).getPayload().toJSONObject()));
            } catch (ParseException e) {
                log.error("Error parsing access token", e);
            }
            Individual individual = individualRepository.findByUserid(oidcUser.getPreferredUsername()).orElseThrow();
            return new PandasUser(individual, oidcUser, mappedAuthorities);
        };
    }
}

