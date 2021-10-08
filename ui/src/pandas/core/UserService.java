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

    public Individual getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Long individualId = ((PandasUser) authentication.getPrincipal()).getIndividualId();
        return individualRepository.findById(individualId).orElseThrow();
    }

    @Override
    public Optional<Individual> getCurrentAuditor() {
        return Optional.ofNullable(getCurrentUser());
    }
}
