package pandas.core;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import pandas.agency.User;
import pandas.agency.UserRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class PandasUserDetailsService implements UserDetailsService, AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
    private final UserRepository userRepository;
    private final Set<String> ALLOWED_LOGIN_ROLE_TYPES = Set.of("PanAdmin", "SysAdmin", "AgAdmin", "StdUser", "SuppUser", "InfoUser");

    public PandasUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserid(username).orElseThrow(() -> new UsernameNotFoundException(username + " has no individual record"));
        if (user == null || user.getRole() == null || !ALLOWED_LOGIN_ROLE_TYPES.contains(user.getRole().getType())) {
            throw new UsernameNotFoundException(username);
        }
        return new PandasUserDetails(user, null, authoritiesFor(user));
    }

    static Collection<? extends GrantedAuthority> authoritiesFor(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        switch (user.getRole().getType()) {
            case "SysAdmin":
                authorities.add(new SimpleGrantedAuthority("ROLE_sysadmin"));
                authorities.addAll(Privileges.byRole.get("sysadmin"));
                // fallthrough
            case "PanAdmin":
                authorities.add(new SimpleGrantedAuthority("ROLE_panadmin"));
                authorities.addAll(Privileges.byRole.get("panadmin"));
                // fallthrough
            case "AgAdmin":
                authorities.add(new SimpleGrantedAuthority("ROLE_agadmin"));
                authorities.addAll(Privileges.byRole.get("agadmin"));
                // fallthrough
            case "StdUser":
                authorities.add(new SimpleGrantedAuthority("ROLE_stduser"));
                authorities.addAll(Privileges.byRole.get("stduser"));
                // fallthrough
            case "SuppUser":
                authorities.add(new SimpleGrantedAuthority("ROLE_suppuser"));
                authorities.addAll(Privileges.byRole.get("suppuser"));
                // fallthrough
            case "InfoUser":
                authorities.add(new SimpleGrantedAuthority("ROLE_infouser"));
                authorities.addAll(Privileges.byRole.get("infouser"));
                break;
            default:
                throw new IllegalStateException("Unknown role type: " + user.getRole().getType());
        }
        return authorities;
    }

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
        return loadUserByUsername(token.getName());
    }
}
