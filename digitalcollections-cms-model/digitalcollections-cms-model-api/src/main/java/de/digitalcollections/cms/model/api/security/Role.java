package de.digitalcollections.cms.model.api.security;

import java.io.Serializable;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;

/**
 * A user's role.
 *
 * @param <ID> unique id specifying instance
 */
public interface Role<ID extends Serializable> extends Identifiable<ID>, GrantedAuthority {

  public static final String PREFIX = "ROLE_";

  List<Operation> getAllowedOperations();

  void setAllowedOperations(List<Operation> allowedOperations);

  String getName();

  void setName(String name);
}
