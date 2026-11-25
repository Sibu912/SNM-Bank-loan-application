package io.github.sibu912.snmbank.loans.security;

import io.github.sibu912.snmbank.loans.domain.Reviewer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * A reviewer as Spring Security sees them: username, password hash and roles.
 */
public class ReviewerPrincipal implements UserDetails {

    private final String username;
    private final String displayName;
    private final String passwordHash;
    private final boolean enabled;

    public ReviewerPrincipal(String username, String displayName, String passwordHash, boolean enabled) {
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
    }

    public static ReviewerPrincipal from(Reviewer reviewer) {
        return new ReviewerPrincipal(
                reviewer.getUsername(), reviewer.getDisplayName(), reviewer.getPasswordHash(), reviewer.isEnabled());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_REVIEWER"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String getDisplayName() {
        return displayName;
    }
}
