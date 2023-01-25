package pandas.agency;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pandas.core.PandasUserDetails;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements AuditorAware<User> {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Long individualId = ((PandasUserDetails) authentication.getPrincipal()).getIndividualId();
        return userRepository.findById(individualId).orElseThrow();
    }

    @Override
    public Optional<User> getCurrentAuditor() {
        return Optional.ofNullable(getCurrentUser());
    }

    public String getEffectiveRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var authorities = authentication.getAuthorities();
        for (String role: List.of("sysadmin", "panadmin", "agadmin", "stduser", "suppuser", "infouser")) {
            if (authorities.contains(new SimpleGrantedAuthority("ROLE_" + role))) {
                return role;
            }
        }
        return "unknown";
    }
}
