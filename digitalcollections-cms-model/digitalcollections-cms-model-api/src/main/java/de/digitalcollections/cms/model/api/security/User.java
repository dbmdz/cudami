package de.digitalcollections.cms.model.api.security;

import java.io.Serializable;
import java.util.List;

/**
 * An user of the system.
 *
 * @param <ID> unique id specifying instance
 */
public interface User<ID extends Serializable> extends Identifiable<ID> {

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
