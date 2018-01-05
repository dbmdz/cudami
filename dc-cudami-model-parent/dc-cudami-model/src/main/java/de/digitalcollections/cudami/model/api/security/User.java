package de.digitalcollections.cudami.model.api.security;

import de.digitalcollections.cudami.model.api.security.enums.Role;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;

/**
 * An user of the system.
 */
public interface User {

  String getEmail();

  void setEmail(String email);

  String getFirstname();

  void setFirstname(String firstname);

  String getLastname();

  void setLastname(String lastname);

  String getPasswordHash();

  void setPasswordHash(String passwordHash);

  List<? extends GrantedAuthority> getRoles();

  void setRoles(List<Role> roles);

  boolean isEnabled();

  void setEnabled(boolean enabled);

  UUID getUuid();

  void setUuid(UUID uuid);
}
