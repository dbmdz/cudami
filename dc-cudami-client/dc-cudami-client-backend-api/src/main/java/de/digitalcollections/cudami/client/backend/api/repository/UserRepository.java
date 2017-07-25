package de.digitalcollections.cudami.client.backend.api.repository;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.security.User;
import java.io.Serializable;
import java.util.List;

/**
 * Repository for User persistence handling.
 * @param <U> implementation of User interface
 * @param <S> unique identifier of user
 */
public interface UserRepository<U extends User, S extends Serializable> {

  User create();

  PageResponse<U> find(PageRequest pageRequest);

  List<U> findAll();

  List<U> findActiveAdminUsers();

  U findByEmail(String email);

  U findOne(S id);

  U save(U user);

  U update(U user);
}
