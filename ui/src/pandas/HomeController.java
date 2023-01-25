package pandas;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.unbescape.html.HtmlEscape;
import org.unbescape.html.HtmlEscapeLevel;
import org.unbescape.html.HtmlEscapeType;
import pandas.core.PandasUserDetailsService;

import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;

@Controller
public class HomeController {
    @GetMapping("/whoami")
    @ResponseBody
    public Principal whoami(Principal principal) {
        return principal;
    }

    public String formatStackTrace(String trace) {
        if (trace == null) return null;
        return trace.replaceAll("(?m)^\\s+at (javax\\.servlet|org\\.springframework|org\\.thymeleaf|org\\.apache|org\\.attoparser)\\..*\n", "")
                .replaceAll("(?m)^\\s+... \\d+ more\n", "");
    }

    @NotNull
    private String escapeHtml(String text) {
        return HtmlEscape.escapeHtml(text, HtmlEscapeType.HEXADECIMAL_REFERENCES, HtmlEscapeLevel.LEVEL_2_ALL_NON_ASCII_PLUS_MARKUP_SIGNIFICANT);
    }

    /**
     * Bit of a hack to let admins see what things look like as another role.
     */
    @GetMapping("/sudo")
    @PreAuthorize("hasAuthority('PRIV_SUDO')")
    public String sudo(@RequestParam("role") String role, Authentication auth, @RequestHeader("referer") String referer,
                       HttpServletRequest request, HttpServletResponse response,
                       @Autowired HttpSessionSecurityContextRepository securityContextRepository) {
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
            case "stduser":
            case "infouser":
                newAuthorities.addAll(PandasUserDetailsService.authoritiesForRoleType(role));
                break;
            default: throw new IllegalArgumentException("this only support infouser, stduser and agadmin currently");
        }
        newAuthorities.add(new SimpleGrantedAuthority("IS_MASQUERADING"));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new AuthenticationWrapper(auth, newAuthorities));
        securityContextRepository.saveContext(context, request, response);

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
