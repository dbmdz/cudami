package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.core.model.api.Sorting;
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

  T createAdminUser();

  T deactivate(ID id);

  boolean doesActiveAdminUserExist();

  List<T> findActiveAdminUsers();

  T get(ID id);

  List<T> getAll(Sorting sorting);

  List<T> getAll();

  T loadUserByUsername(String string) throws UsernameNotFoundException;

  T save(T user, Errors results);

  T update(T user, Errors results);
}
