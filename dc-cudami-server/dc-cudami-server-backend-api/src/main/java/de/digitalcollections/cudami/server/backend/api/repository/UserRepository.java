package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.security.User;
import java.util.List;
import java.util.UUID;

/**
 * Repository for User persistence handling.
 *
 * @param <U> entity instance
 */
public interface UserRepository<U extends User> {

  long count();

  U create();

  PageResponse<U> find(PageRequest pageRequest);

  U findByEmail(String email);

  U findOne(UUID uuid);

  List<U> findActiveAdminUsers();

  U save(U user);

  U update(U user);
}
