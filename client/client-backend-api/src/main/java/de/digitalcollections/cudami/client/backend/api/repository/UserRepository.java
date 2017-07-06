package de.digitalcollections.cudami.client.backend.api.repository;

import de.digitalcollections.cudami.model.api.security.User;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for User persistence handling.
 * @param <T> implementation of User interface
 */
public interface UserRepository<T extends User> extends PagingAndSortingRepository<T, Long> {

  User create();

  @Override
  List<T> findAll(Sort sort);

  T findByEmail(String email);

  List<T> findActiveAdminUsers();
}
