package pandas;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pandas.core.Privileges;

import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Controller
public class HomeController {
    @GetMapping("/whoami")
    @ResponseBody
    public Principal whoami(Principal principal) {
        return principal;
    }

    /**
     * Bit of a hack to let admins see what things look like as another role.
     */
    @GetMapping("/sudo")
    @PreAuthorize("hasAuthority('PRIV_SUDO')")
    public String sudo(@RequestParam("role") String role, Authentication auth, @RequestHeader("referer") String referer) {
        var newAuthorities = new HashSet<GrantedAuthority>();
        for (var authority : auth.getAuthorities()) {
            if (authority instanceof SimpleGrantedAuthority) {
                if (authority.getAuthority().startsWith("ROLE_") || authority.getAuthority().startsWith("PRIV_")) {
                    continue; // remove it
                }
            }
            newAuthorities.add(authority);
        }
        switch (role) {
            case "agadmin":
                newAuthorities.add(new SimpleGrantedAuthority("ROLE_agadmin"));
                newAuthorities.addAll(Privileges.byRole.getOrDefault("agadmin", Set.of()));
            case "stduser":
                newAuthorities.add(new SimpleGrantedAuthority("ROLE_stduser"));
                newAuthorities.addAll(Privileges.byRole.getOrDefault("stduser", Set.of()));
                break;
            default: throw new IllegalArgumentException("this only support stduser and agadmin currently");
        }
        SecurityContextHolder.getContext().setAuthentication(new AuthenticationWrapper(auth, newAuthorities));
        return "redirect:" + (referer != null ? referer : "/");
    }

    public static class AuthenticationWrapper implements Authentication, CredentialsContainer {
        private final Authentication delegate;
        private final HashSet<GrantedAuthority> authorities;

        public AuthenticationWrapper(Authentication delegate, HashSet<GrantedAuthority> authorities) {
            if (delegate instanceof AuthenticationWrapper) {
                this.delegate = ((AuthenticationWrapper)delegate).delegate;
            } else {
                this.delegate = delegate;
            }
            this.authorities = authorities;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public Object getCredentials() {
            return delegate.getCredentials();
        }

        @Override
        public Object getDetails() {
            return delegate.getDetails();
        }

        @Override
        public Object getPrincipal() {
            return delegate.getPrincipal();
        }

        @Override
        public boolean isAuthenticated() {
            return delegate.isAuthenticated();
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            delegate.setAuthenticated(isAuthenticated);
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public void eraseCredentials() {
            if (delegate instanceof CredentialsContainer) {
                ((CredentialsContainer) delegate).eraseCredentials();
            }
        }
    }
}
