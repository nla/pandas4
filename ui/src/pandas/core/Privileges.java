package pandas.core;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Map;
import java.util.Set;

public class Privileges {
    public static final SimpleGrantedAuthority NOMINATE_TITLES = new SimpleGrantedAuthority("PRIV_NOMINATE_TITLES");
    public static final SimpleGrantedAuthority SELECT_TITLES = new SimpleGrantedAuthority("PRIV_SELECT_TITLES");
    public static final SimpleGrantedAuthority EDIT_OWN_TITLES = new SimpleGrantedAuthority("PRIV_EDIT_OWN_TITLES");
    public static final SimpleGrantedAuthority EDIT_AGENCY_TITLES = new SimpleGrantedAuthority("PRIV_EDIT_AGENCY_TITLES");
    public static final SimpleGrantedAuthority EDIT_ALL_TITLES = new SimpleGrantedAuthority("PRIV_EDIT_ALL_TITLES");
    public static final SimpleGrantedAuthority BULK_EDIT_TITLES = new SimpleGrantedAuthority("PRIV_BULK_EDIT_TITLES");
    public static final SimpleGrantedAuthority BULK_EDIT_TITLES_ADVANCED = new SimpleGrantedAuthority("PRIV_BULK_EDIT_TITLES_ADVANCED");

    public static final SimpleGrantedAuthority EDIT_AGENCY_USERS = new SimpleGrantedAuthority("PRIV_EDIT_AGENCY_USERS");
    public static final SimpleGrantedAuthority EDIT_ALL_USERS = new SimpleGrantedAuthority("PRIV_EDIT_ALL_USERS");


    public static final SimpleGrantedAuthority EDIT_OWN_AGENCY = new SimpleGrantedAuthority("PRIV_EDIT_OWN_AGENCY");
    public static final SimpleGrantedAuthority EDIT_ALL_AGENCIES = new SimpleGrantedAuthority("PRIV_EDIT_ALL_AGENCIES");

    public static final SimpleGrantedAuthority EDIT_COLLECTIONS = new SimpleGrantedAuthority("PRIV_EDIT_COLLECTIONS");
    public static final SimpleGrantedAuthority EDIT_PUBLISHERS = new SimpleGrantedAuthority("PRIV_EDIT_PUBLISHERS");
    public static final SimpleGrantedAuthority EDIT_SUBJECTS = new SimpleGrantedAuthority("PRIV_EDIT_SUBJECTS");
    public static final SimpleGrantedAuthority EDIT_DISCOVERY_SOURCES = new SimpleGrantedAuthority("PRIV_EDIT_DISCOVERY_SOURCES");


    public static final SimpleGrantedAuthority VIEW_ALL_AGENCY_WORKTRAYS  = new SimpleGrantedAuthority("PRIV_VIEW_ALL_AGENCY_WORKTRAYS");
    public static final SimpleGrantedAuthority VIEW_GATHER_QUEUE = new SimpleGrantedAuthority("PRIV_VIEW_GATHER_QUEUE");
    public static final SimpleGrantedAuthority ADMIN_GATHER_OPTIONS = new SimpleGrantedAuthority("PRIV_ADMIN_GATHER_OPTIONS");
    public static final SimpleGrantedAuthority CONTROL_GATHERER = new SimpleGrantedAuthority("PRIV_CONTROL_GATHERER");
    public static final SimpleGrantedAuthority SUDO = new SimpleGrantedAuthority("PRIV_SUDO");
    public static final SimpleGrantedAuthority SYSADMIN = new SimpleGrantedAuthority("PRIV_SYSADMIN");

    public static final Map<String, Set<GrantedAuthority>> byRole = Map.of(
            "infouser", Set.of(NOMINATE_TITLES, EDIT_COLLECTIONS, EDIT_OWN_TITLES),
            "suppuser", Set.of(),
            "stduser", Set.of(SELECT_TITLES, EDIT_PUBLISHERS, VIEW_GATHER_QUEUE, EDIT_AGENCY_TITLES),
            "agadmin", Set.of(EDIT_AGENCY_USERS, EDIT_OWN_AGENCY, BULK_EDIT_TITLES),
            "panadmin", Set.of(EDIT_ALL_TITLES, EDIT_ALL_USERS, BULK_EDIT_TITLES_ADVANCED,
                    EDIT_SUBJECTS, ADMIN_GATHER_OPTIONS, SUDO, EDIT_DISCOVERY_SOURCES, EDIT_ALL_AGENCIES,
                    VIEW_ALL_AGENCY_WORKTRAYS, CONTROL_GATHERER),
            "sysadmin", Set.of(SYSADMIN));
}
