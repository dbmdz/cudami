package de.digitalcollections.cms.client.backend.api.repository;

import de.digitalcollections.cms.model.api.security.Role;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for Role persistence handling.
 * @param <T> implementation of Role interface
 */
public interface RoleRepository<T extends Role> extends PagingAndSortingRepository<T, Long> {

  T create();

  @Override
  List<T> findAll(Sort sort);

  T findByName(String name);

}
