package pandas.collection;

import org.springframework.stereotype.Component;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.agency.User;
import pandas.agency.UserRepository;
import pandas.agency.UserService;
import pandas.gather.InstanceRepository;

import java.util.Map;

@Component
public class SidebarService {
    private final InstanceRepository instanceRepository;
    private final TitleRepository titleRepository;
    private final UserService userService;
    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;

    public SidebarService(InstanceRepository instanceRepository,
                          TitleRepository titleRepository,
                          UserService userService,
                          AgencyRepository agencyRepository,
                          UserRepository userRepository) {
        this.instanceRepository = instanceRepository;
        this.titleRepository = titleRepository;
        this.userService = userService;
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
    }

    public long nominated(Map<String, Object> session) {
        Agency agency = nominatedAgency(session);
        return titleRepository.countWorktrayNominated(agency.getId());
    }

    private Agency nominatedAgency(Map<String, Object> session) {
        String lastAlias = (String) session.get(WorktraysController.LAST_ALIAS);
        if (lastAlias == null || lastAlias.isBlank()) {
            return userService.getCurrentUser().getAgency();
        }
        return agencyRepository.findByAlias(lastAlias)
                .orElseGet(() -> userRepository.findByUserid(lastAlias)
                        .map(User::getAgency)
                        .orElseGet(() -> userService.getCurrentUser().getAgency()));
    }

    public record NominatedItem(String alias, long count) {}

    public long qaCount(Map<String, Object> session) {
        String lastAlias = (String) session.get(WorktraysController.LAST_ALIAS);
        if (lastAlias == null || lastAlias.isBlank()) {
            return instanceRepository.countGatheredWorktray(userService.getCurrentUser().getId());
        }
        return agencyRepository.findByAlias(lastAlias)
                .map(a -> instanceRepository.countGatheredWorktrayByAgency(a.getId()))
                .orElseGet(() -> userRepository.findByUserid(lastAlias)
                        .map(u -> instanceRepository.countGatheredWorktray(u.getId()))
                        .orElseGet(() -> instanceRepository.countGatheredWorktray(userService.getCurrentUser().getId())));
    }

    public String qaLabel(Map<String, Object> session) {
        String lastAlias = (String) session.get(WorktraysController.LAST_ALIAS);
        return (lastAlias == null || lastAlias.isBlank()) ? "" : lastAlias;
    }
}
