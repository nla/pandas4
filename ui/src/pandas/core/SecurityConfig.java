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
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pandas.agency.AgencyRepository;
import pandas.agency.User;
import pandas.agency.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri:#{null}}")
    private String oidcIssuerUri;

    private final PandasUserDetailsService pandasUserDetailsService;
    private final Config config;

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final LinkedAccountRepository linkedAccountRepository;
    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;


    public SecurityConfig(PandasUserDetailsService pandasUserDetailsService, Config config,
                          @Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository, LinkedAccountRepository linkedAccountRepository, UserRepository userRepository, AgencyRepository agencyRepository) {
        this.pandasUserDetailsService = pandasUserDetailsService;
        this.config = config;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.linkedAccountRepository = linkedAccountRepository;
        this.userRepository = userRepository;
        this.agencyRepository = agencyRepository;
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
        http.mvcMatcher("/login/check-session-reply").headers().frameOptions().sameOrigin().and().mvcMatcher("/**");
        if (oidcIssuerUri != null) {
            System.out.println("setting user service");
            http.oauth2Login().userInfoEndpoint().oidcUserService(oidcUserService())
                    .and().loginPage("/login")
                    .and().logout().logoutUrl("/logout");
            http.logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
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
                .mvcMatchers("/login/check-session-reply").permitAll()
                .mvcMatchers("/logout").permitAll()
                .mvcMatchers("/assets/**").permitAll()
                .mvcMatchers("/whoami").permitAll()
                .anyRequest().hasAnyRole("infouser");
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

            String provider = userRequest.getClientRegistration().getRegistrationId();
            String externalId = userRequest.getIdToken().getSubject();

            LinkedAccount linkedAccount = linkedAccountRepository.findByProviderAndExternalId(provider, externalId).orElse(null);
            if (linkedAccount != null) {
                linkedAccount.setLastLoginDate(Instant.now());
                linkedAccountRepository.save(linkedAccount);
                mappedAuthorities.addAll(PandasUserDetailsService.authoritiesFor(linkedAccount.getUser()));
                return new PandasUserDetails(linkedAccount.getUser(), oidcUser, mappedAuthorities);
            }

            String userid = oidcUser.getPreferredUsername().toLowerCase(Locale.ROOT);

            // FIXME: which providers autoregister and autolink should be configurable
            if (!provider.equals("shire") && !provider.equals("oidc")) {
                throw new OAuth2AuthenticationException("Account auto-linking only enabled for 'oidc' and 'shire' providers");
            }
            User user = userRepository.findByUserid(userid).orElse(null);
            if (linkedAccountRepository.existsByUserAndProvider(user, provider)) {
                throw new OAuth2AuthenticationException("Username already linked to a different account");
            }

            if (user == null) {
                Role role = new Role();
                role.setOrganisation(agencyRepository.findById(1L).orElseThrow().getOrganisation());
                role.setType("InfoUser");
                role.setTitle("Informational User");

                user = new User();
                user.setUserid(userid);
                user.setNameGiven(userRequest.getIdToken().getGivenName());
                user.setNameFamily(userRequest.getIdToken().getFamilyName());
                user.setEmail(userRequest.getIdToken().getEmail());
                user.setPhone(userRequest.getIdToken().getPhoneNumber());
                user.setActive(true);
                user.setRole(role);
                user = userRepository.save(user);
            }

            linkedAccount = new LinkedAccount();
            linkedAccount.setUser(user);
            linkedAccount.setProvider(provider);
            linkedAccount.setExternalId(userRequest.getIdToken().getSubject());
            linkedAccount.setLastLoginDate(Instant.now());
            linkedAccountRepository.save(linkedAccount);

            mappedAuthorities.addAll(PandasUserDetailsService.authoritiesFor(user));
            return new PandasUserDetails(user, oidcUser, mappedAuthorities);
        };
    }
}

