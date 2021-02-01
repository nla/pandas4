package pandas.core;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class PandasUserDetailsService implements UserDetailsService, AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
    private final IndividualRepository individualRepository;
    private final Set<String> ALLOWED_LOGIN_ROLE_TYPES = Set.of("PanAdmin", "SysAdmin", "AgAdmin", "StdUser", "SuppUser", "InfoUser");

    public PandasUserDetailsService(IndividualRepository individualRepository) {
        this.individualRepository = individualRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Individual individual = individualRepository.findByUserid(username);
        if (individual == null || individual.getRole() == null || !ALLOWED_LOGIN_ROLE_TYPES.contains(individual.getRole().getType())) {
            throw new UsernameNotFoundException(username);
        }
        return new PandasUser(individual);
    }

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
        return loadUserByUsername(token.getName());
    }

    public static class PandasUser implements UserDetails {
        private final Individual individual;

        public PandasUser(Individual individual) {
            this.individual = individual;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            Set<GrantedAuthority> authorities = new HashSet<>();
            switch (individual.getRole().getType()) {
                case "SysAdmin":
                    authorities.add(new SimpleGrantedAuthority("ROLE_sysadmin"));
                    // fallthrough
                case "PanAdmin":
                    authorities.add(new SimpleGrantedAuthority("ROLE_panadmin"));
                    // fallthrough
                case "AgAdmin":
                    authorities.add(new SimpleGrantedAuthority("ROLE_agadmin"));
                    // fallthrough
                case "StdUser":
                    authorities.add(new SimpleGrantedAuthority("ROLE_stduser"));
                    // fallthrough
                case "SuppUser":
                    authorities.add(new SimpleGrantedAuthority("ROLE_suppuser"));
                    // fallthrough
                case "InfoUser":
                    authorities.add(new SimpleGrantedAuthority("ROLE_infouser"));
                    break;
                default:
                    throw new IllegalStateException("Unknown role type: " + individual.getRole().getType());
            }
            return authorities;
        }

        @Override
        public String getPassword() {
            return individual.getPassword();
        }

        @Override
        public String getUsername() {
            return individual.getUserid();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
