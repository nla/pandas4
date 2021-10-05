package pandas.core;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.collection.Title;
import pandas.collection.TitleRepository;

import java.io.Serializable;

import static pandas.core.Privileges.*;

@SuppressWarnings({"RedundantIfStatement"})
@Component
public class PandasPermissionEvaluator implements PermissionEvaluator {
    private final AgencyRepository agencyRepository;
    private final IndividualRepository individualRepository;
    private final TitleRepository titleRepository;

    public PandasPermissionEvaluator(AgencyRepository agencyRepository, IndividualRepository individualRepository, TitleRepository titleRepository) {
        this.agencyRepository = agencyRepository;
        this.individualRepository = individualRepository;
        this.titleRepository = titleRepository;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object target, Object permission) {
        var authorities = authentication.getAuthorities();
        switch (target.getClass().getSimpleName() + ":" + permission) {
            case "Agency:edit": {
                if (authorities.contains(EDIT_ALL_AGENCIES)) {
                    return true;
                }
                Agency agency = (Agency) target;
                Individual currentUser = individualRepository.findByUserid(authentication.getName()).orElseThrow();
                if (authorities.contains(EDIT_OWN_AGENCY) && currentUser.getAgency().equals(agency)) {
                    return true;
                }
                return false;
            }
            case "Collection:edit":
                return authorities.contains(EDIT_COLLECTIONS);
            case "Individual:edit": {
                if (authorities.contains(EDIT_ALL_USERS)) {
                    return true;
                }
                Individual targetUser = (Individual) target;
                if (targetUser.getUserid() == null) {
                    return false; // not an actual user
                }
                Individual currentUser = individualRepository.findByUserid(authentication.getName()).orElseThrow();
                if (authorities.contains(EDIT_AGENCY_USERS) && currentUser.getAgency().equals(targetUser.getAgency())) {
                    return true;
                }
                return false;
            }
            case "Publisher:edit":
                return authorities.contains(EDIT_PUBLISHERS);
            case "Subject:edit":
                return authorities.contains(EDIT_SUBJECTS);
            case "Title:edit": {
                if (authorities.contains(EDIT_ALL_TITLES))
                    return true;
                Title title = (Title) target;
                var user = individualRepository.findByUserid(authentication.getName()).orElseThrow();
                if (authorities.contains(EDIT_OWN_TITLES) && title.isOwnedBy(user))
                    return true;
                if (authorities.contains(EDIT_AGENCY_TITLES) && title.isOwnedBy(user.getAgency()))
                    return true;
                return false;
            }
            default:
                throw new IllegalArgumentException("Unknown permission " + target.getClass().getSimpleName() + ":" + permission);
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        var authorities = authentication.getAuthorities();
        switch (targetType + ":" + permission) {
            case "Agency:edit": {
                if (targetId == null) {
                    return authorities.contains(EDIT_ALL_AGENCIES);
                }
                Agency agency = agencyRepository.findById((Long)targetId).orElseThrow(NotFoundException::new);
                return hasPermission(authentication, agency, permission);
            }
            case "Collection:edit":
                return authorities.contains(EDIT_COLLECTIONS);
            case "Individual:edit": {
                if (targetId == null) {
                    return authorities.contains(EDIT_ALL_USERS); // TODO: agadmins probably need to create users
                }
                Individual user = individualRepository.findById((Long)targetId).orElseThrow(NotFoundException::new);
                return hasPermission(authentication, user, permission);
            }
            case "Publisher:edit":
                return authorities.contains(EDIT_PUBLISHERS);
            case "Subject:edit":
                return authorities.contains(EDIT_SUBJECTS);
            case "Title:edit": {
                if (targetId == null)
                    return authorities.contains(NOMINATE_TITLES);
                Title title = titleRepository.findById((Long)targetId).orElseThrow(NotFoundException::new);
                return hasPermission(authentication, title, permission);
            }
            default:
                throw new IllegalArgumentException("Unknown permission " + targetType + "(" + targetId + "):" + permission);
        }
    }
}
