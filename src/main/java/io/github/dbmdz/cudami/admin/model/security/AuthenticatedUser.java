package io.github.dbmdz.cudami.admin.model.security;

import de.digitalcollections.model.security.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedUser implements UserDetails {

  private static final long serialVersionUID = 1L;

  private final User user;

  @SuppressFBWarnings
  public AuthenticatedUser(final User user) {
    this.user = user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getRoles().stream()
        .map(
            r -> {
              return (GrantedAuthority)
                  () -> {
                    // Prefix "ROLE_" needed by Spring Security
                    return "ROLE_" + r.name();
                  };
            })
        .collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return user.getPasswordHash();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
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
    return user.isEnabled();
  }
}
