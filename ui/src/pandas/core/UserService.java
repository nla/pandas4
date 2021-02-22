package pandas.core;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final IndividualRepository individualRepository;

    public UserService(IndividualRepository individualRepository) {
        this.individualRepository = individualRepository;
    }

    public Individual getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        String username = authentication.getName();
        if (username == null) {
            return null;
        }
        return individualRepository.findByUserid(username).orElse(null);
    }
}
