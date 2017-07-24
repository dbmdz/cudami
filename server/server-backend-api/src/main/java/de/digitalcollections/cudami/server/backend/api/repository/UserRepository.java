package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.security.User;
import java.io.Serializable;
import java.util.List;

/**
 * Repository for User persistence handling.
 *
 * @param <U> entity instance
 * @param <ID> unique id
 */
public interface UserRepository<U extends User, ID extends Serializable> {

  long count();

  U create();

  PageResponse<U> find(PageRequest pageRequest);

  U findByEmail(String email);

  U findOne(ID id);

  List<U> findActiveAdminUsers();

  U save(U user);

  U update(U user);
}
