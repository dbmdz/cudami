package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.core.model.api.Sorting;
import de.digitalcollections.cudami.model.api.security.User;
import java.io.Serializable;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for User persistence handling.
 *
 * @param <T> entity instance
 * @param <ID> unique id
 */
public interface UserRepository<T extends User, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {

  T create();

  List<T> findAll(Sorting sorting);

  T findByEmail(String email);

  List<T> findActiveAdminUsers();
}
