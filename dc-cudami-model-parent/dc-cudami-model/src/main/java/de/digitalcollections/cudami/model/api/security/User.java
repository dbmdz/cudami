package de.digitalcollections.cudami.model.api.security;

import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import de.digitalcollections.cudami.model.api.security.enums.Role;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;

/**
 * An user of the system.
 */
public interface User extends Identifiable {

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
}
