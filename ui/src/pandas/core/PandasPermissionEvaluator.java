package pandas.core;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.agency.User;
import pandas.agency.UserRepository;
import pandas.collection.Title;
import pandas.collection.TitleRepository;

import java.io.Serializable;

import static pandas.core.Privileges.*;

@SuppressWarnings({"RedundantIfStatement"})
@Component
public class PandasPermissionEvaluator implements PermissionEvaluator {
    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final TitleRepository titleRepository;

    public PandasPermissionEvaluator(AgencyRepository agencyRepository, UserRepository userRepository, TitleRepository titleRepository) {
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
        this.titleRepository = titleRepository;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object target, Object permission) {
        var authorities = authentication.getAuthorities();
        switch (target.getClass().getSimpleName() + ":" + permission) {
            case "Agency:create-user": {
                if (authorities.contains(EDIT_ALL_USERS)) {
                    return true;
                }
                Agency agency = (Agency) target;
                User currentUser = userRepository.findByUserid(authentication.getName()).orElseThrow();
                if (authorities.contains(EDIT_AGENCY_USERS) && currentUser.getAgency().equals(agency)) {
                    return true;
                }
                return false;
            }
            case "Agency:edit": {
                if (authorities.contains(EDIT_ALL_AGENCIES)) {
                    return true;
                }
                Agency agency = (Agency) target;
                User currentUser = userRepository.findByUserid(authentication.getName()).orElseThrow();
                if (authorities.contains(EDIT_OWN_AGENCY) && currentUser.getAgency().equals(agency)) {
                    return true;
                }
                return false;
            }
            case "Collection:edit":
                return authorities.contains(EDIT_COLLECTIONS);
            case "Publisher:edit":
                return authorities.contains(EDIT_PUBLISHERS);
            case "Subject:edit":
                return authorities.contains(EDIT_SUBJECTS);
            case "Title:edit": {
                if (authorities.contains(EDIT_ALL_TITLES))
                    return true;
                Title title = (Title) target;
                var user = userRepository.findByUserid(authentication.getName()).orElseThrow();
                if (authorities.contains(EDIT_OWN_TITLES) && title.isOwnedBy(user))
                    return true;
                if (authorities.contains(EDIT_AGENCY_TITLES) && title.isOwnedBy(user.getAgency()))
                    return true;
                return false;
            }
            case "User:edit": {
                if (authorities.contains(EDIT_ALL_USERS)) {
                    return true;
                }
                User targetUser = (User) target;
                if (targetUser.getUserid() == null) {
                    return false; // not an actual user
                }
                User currentUser = userRepository.findByUserid(authentication.getName()).orElseThrow();
                if (authorities.contains(EDIT_AGENCY_USERS) && currentUser.getAgency().equals(targetUser.getAgency())) {
                    return true;
                }
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
            case "Agency:create-user": {
                Agency agency = agencyRepository.findById((Long)targetId).orElseThrow(NotFoundException::new);
                return hasPermission(authentication, agency, permission);
            }
            case "Agency:edit": {
                if (targetId == null) {
                    return authorities.contains(EDIT_ALL_AGENCIES);
                }
                Agency agency = agencyRepository.findById((Long)targetId).orElseThrow(NotFoundException::new);
                return hasPermission(authentication, agency, permission);
            }
            case "Collection:edit":
                return authorities.contains(EDIT_COLLECTIONS);
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
            case "User:edit": {
                if (targetId == null) {
                    return authorities.contains(EDIT_ALL_USERS); // TODO: agadmins probably need to create users
                }
                User user;
                if (targetId instanceof String userid) {
                    user = userRepository.findByUserid(userid).orElseThrow(NotFoundException::new);
                } else if (targetId instanceof Long individualId) {
                    user = userRepository.findById(individualId).orElseThrow(NotFoundException::new);
                } else {
                    throw new IllegalArgumentException("got " + targetId.getClass() + " targetId but expected String or Long");
                }
                return hasPermission(authentication, user, permission);
            }
            default:
                throw new IllegalArgumentException("Unknown permission " + targetType + "(" + targetId + "):" + permission);
        }
    }
}
