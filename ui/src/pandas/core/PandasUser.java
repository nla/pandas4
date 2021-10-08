package pandas.core;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class PandasUser implements UserDetails, OidcUser {
    private final Long individualId;
    private final String username;
    private transient final String password;
    private final OidcUser oidcUser;
    private final Collection<? extends GrantedAuthority> authorities;

    public PandasUser(Individual individual, OidcUser oidcUser, Collection<? extends GrantedAuthority> authorities) {
        this.individualId = individual.getId();
        this.username = individual.getUserid();
        this.oidcUser = oidcUser;
        this.authorities = authorities;
        this.password = oidcUser == null ? individual.getPassword() : null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser == null ? Collections.emptyMap() : oidcUser.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser == null ? Collections.emptyMap() : oidcUser.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser == null ? null : oidcUser.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser == null ? null : oidcUser.getIdToken();
    }

    @Override
    public String getName() {
        return getUsername();
    }

    public Long getIndividualId() {
        return individualId;
    }
}
