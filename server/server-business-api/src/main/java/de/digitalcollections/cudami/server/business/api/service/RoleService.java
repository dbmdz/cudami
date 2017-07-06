package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.cudami.model.api.security.Role;
import java.io.Serializable;
import java.util.List;

/**
 * @param <T> domain object
 * @param <ID> unique id
 */
public interface RoleService<T extends Role, ID extends Serializable> {

  T create();

  T getAdminRole();

  T get(ID id);

  List<T> getAll();

  public Role findByName(String name);
}
