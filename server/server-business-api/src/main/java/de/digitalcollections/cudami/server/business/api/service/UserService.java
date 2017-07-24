package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
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

  PageResponse<T> find(PageRequest pageRequest);

  T loadUserByUsername(String string) throws UsernameNotFoundException;

  T save(T user, Errors results);

  T update(T user, Errors results);
}
