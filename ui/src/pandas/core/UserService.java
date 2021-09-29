package pandas.core;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements AuditorAware<Individual> {
    private final IndividualRepository individualRepository;

    public UserService(IndividualRepository individualRepository) {
        this.individualRepository = individualRepository;
    }

    public static Individual currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return ((PandasUser)authentication.getPrincipal()).getIndividual();
    }

    public Individual getCurrentUser() {
        return currentUser();
    }

    @Override
    public Optional<Individual> getCurrentAuditor() {
        return Optional.ofNullable(getCurrentUser());
    }
}
