package de.digitalcollections.cudami.admin.backend.api.repository.security;

import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.security.User;
import java.util.List;
import java.util.UUID;

/**
 * Repository for User persistence handling.
 *
 * @param <U> implementation of User interface
 */
public interface UserRepository<U extends User> {

  long count();

  U create();

  PageResponse<U> find(PageRequest pageRequest);

  U findOne(UUID uuid);

  U save(U user);

  U update(U user);

  List<U> findAll();

  List<U> findActiveAdminUsers();

  U findByEmail(String email);
}
