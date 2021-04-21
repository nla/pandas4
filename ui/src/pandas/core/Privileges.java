package pandas.core;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Map;
import java.util.Set;

public class Privileges {
    public static final SimpleGrantedAuthority CREATE_TITLES = new SimpleGrantedAuthority("PRIV_CREATE_TITLES");
    public static final SimpleGrantedAuthority EDIT_OWN_TITLES = new SimpleGrantedAuthority("PRIV_EDIT_OWN_TITLES");
    public static final SimpleGrantedAuthority EDIT_AGENCY_TITLES = new SimpleGrantedAuthority("PRIV_EDIT_AGENCY_TITLES");
    public static final SimpleGrantedAuthority EDIT_ALL_TITLES = new SimpleGrantedAuthority("PRIV_EDIT_ALL_TITLES");
    public static final SimpleGrantedAuthority BULK_EDIT_TITLES = new SimpleGrantedAuthority("PRIV_BULK_EDIT_TITLES");

    public static final SimpleGrantedAuthority EDIT_COLLECTIONS = new SimpleGrantedAuthority("PRIV_EDIT_COLLECTIONS");
    public static final SimpleGrantedAuthority EDIT_PUBLISHERS = new SimpleGrantedAuthority("PRIV_EDIT_PUBLISHERS");
    public static final SimpleGrantedAuthority EDIT_SUBJECTS = new SimpleGrantedAuthority("PRIV_EDIT_SUBJECTS");

    public static final Map<String, Set<GrantedAuthority>> byRole = Map.of(
            "infouser", Set.of(),
            "suppuser", Set.of(),
            "stduser", Set.of(CREATE_TITLES, EDIT_OWN_TITLES, EDIT_COLLECTIONS, EDIT_PUBLISHERS),
            "agadmin", Set.of(EDIT_AGENCY_TITLES),
            "panadmin", Set.of(EDIT_ALL_TITLES, BULK_EDIT_TITLES, EDIT_SUBJECTS),
            "sysadmin", Set.of());
}
