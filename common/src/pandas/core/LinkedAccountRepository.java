package pandas.core;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pandas.agency.User;

import java.util.Optional;

@Repository
public interface LinkedAccountRepository extends CrudRepository<LinkedAccount, Long> {
    Optional<LinkedAccount> findByProviderAndExternalId(String provider, String externalId);

    boolean existsByUserAndProvider(User user, String provider);
}
