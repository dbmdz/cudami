package de.digitalcollections.cms.server.backend.api.repository;

import de.digitalcollections.cms.model.api.security.Role;
import java.io.Serializable;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for Role persistence handling.
 *
 * @param <T> entity instance
 * @param <ID> unique id
 */
public interface RoleRepository<T extends Role, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {

  T create();

  @Override
  List<T> findAll(Sort sort);

  T findByName(String name);

}
