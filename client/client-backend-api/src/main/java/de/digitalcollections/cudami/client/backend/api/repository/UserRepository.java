package de.digitalcollections.cudami.client.backend.api.repository;

import de.digitalcollections.core.model.api.Sorting;
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

  List<U> findAll(Sorting sorting);

  List<U> findActiveAdminUsers();

  U findByEmail(String email);

  U findOne(S id);

  U save(U user);

  U update(U user);
}
