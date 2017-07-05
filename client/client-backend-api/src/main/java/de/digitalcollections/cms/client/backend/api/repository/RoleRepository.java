package de.digitalcollections.cms.client.backend.api.repository;

import de.digitalcollections.cms.model.api.security.Role;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for Role persistence handling.
 */
public interface RoleRepository extends PagingAndSortingRepository<Role, Long> {

  Role create();

  @Override
  List<Role> findAll(Sort sort);

  Role findByName(String name);

}
