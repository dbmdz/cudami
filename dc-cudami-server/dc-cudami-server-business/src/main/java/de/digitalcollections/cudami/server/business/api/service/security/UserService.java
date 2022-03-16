package de.digitalcollections.cudami.server.business.api.service.security;

import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.security.User;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.Errors;

/**
 * Service for User.
 *
 * @param <T> domain object
 */
public interface UserService<T extends User> {

  T activate(UUID uuid);

  T createAdminUser();

  T deactivate(UUID uuid);

  boolean doesActiveAdminUserExist();

  PageResponse<T> find(PageRequest pageRequest);

  List<T> findActiveAdminUsers();

  T getByUuid(UUID uuid);

  T loadUserByUsername(String string) throws UsernameNotFoundException;

  T save(T user, Errors results);

  T update(T user, Errors results);
}
