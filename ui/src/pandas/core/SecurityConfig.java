package pandas.core;

import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

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

    public SecurityConfig(PandasUserDetailsService pandasUserDetailsService, Config config) {
        this.pandasUserDetailsService = pandasUserDetailsService;
        this.config = config;
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
        http.csrf().disable();
        if (oidcIssuerUri != null) {
            http.oauth2Login().userInfoEndpoint().oidcUserService(oidcUserService());
            http.logout().logoutSuccessUrl("/");
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
                .antMatchers("/actuator/health").anonymous()
                .antMatchers("/collections", "/collections/**").hasRole("panadmin")
                .antMatchers("/gather/**").hasRole("panadmin")
                .antMatchers("/profiles", "/profiles/**").hasRole("panadmin")
                .antMatchers("/schedules", "/schedules/**").hasRole("panadmin")
                .antMatchers("/pageinfo").hasRole("stduser")
                .antMatchers("/crawls/**").hasRole("panadmin")
                .anyRequest().hasRole("stduser");
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
            String usernameAttribute = "preferred_username";
            return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), usernameAttribute);
        };
    }
}

