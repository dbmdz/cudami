package de.digitalcollections.cms.client.backend.api.repository;

import de.digitalcollections.cms.model.api.security.User;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for User persistence handling.
 */
public interface UserRepository extends PagingAndSortingRepository<User, Long> {

  User create();

  @Override
  List<User> findAll(Sort sort);

  User findByEmail(String email);

  List<User> findActiveAdminUsers();
}
