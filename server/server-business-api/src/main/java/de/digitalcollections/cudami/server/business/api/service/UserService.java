package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.cudami.model.api.security.User;
import java.io.Serializable;
import java.util.List;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.Errors;

/**
 * Service for User.
 *
 * @param <T> domain object
 * @param <ID> unique id
 */
public interface UserService<T extends User, ID extends Serializable> {

  T activate(ID id);

  T create();

  T save(T user, Errors results);

  T createAdminUser();

  T deactivate(ID id);

  T get(ID id);

  List<T> getAll();

  T update(T user, Errors results);

  boolean doesActiveAdminUserExist();

  public List<T> findActiveAdminUsers();

  public T loadUserByUsername(String string) throws UsernameNotFoundException;
}
