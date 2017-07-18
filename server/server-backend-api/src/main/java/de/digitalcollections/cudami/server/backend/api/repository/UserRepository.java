package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.core.model.api.Sorting;
import de.digitalcollections.cudami.model.api.security.User;
import java.io.Serializable;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for User persistence handling.
 *
 * @param <U> entity instance
 * @param <ID> unique id
 */
public interface UserRepository<U extends User, ID extends Serializable> extends PagingAndSortingRepository<U, ID> {

  U create();

  List<U> findAll(Sorting sorting);

  U findByEmail(String email);

  List<U> findActiveAdminUsers();

  U update(U user);
}
