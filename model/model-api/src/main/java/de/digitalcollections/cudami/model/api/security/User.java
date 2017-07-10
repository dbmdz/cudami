package de.digitalcollections.cudami.model.api.security;

import de.digitalcollections.cudami.model.api.Identifiable;
import de.digitalcollections.cudami.model.api.security.enums.Role;
import java.io.Serializable;
import java.util.List;

/**
 * An user of the system.
 * @param <T> unique serializable identifier
 */
public interface User<T extends Serializable> extends Identifiable<T> {

  String getEmail();

  void setEmail(String email);

  String getFirstname();

  void setFirstname(String firstname);

  String getLastname();

  void setLastname(String lastname);

  String getPasswordHash();

  void setPasswordHash(String passwordHash);

  List<Role> getRoles();

  void setRoles(List<Role> roles);

  boolean isEnabled();

  void setEnabled(boolean enabled);
}
