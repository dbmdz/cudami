package de.digitalcollections.cms.client.backend.api.repository;

import de.digitalcollections.cms.model.api.entity.Website;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for Website persistence handling.
 */
public interface WebsiteRepository extends PagingAndSortingRepository<Website, Long> {

  Website create();

  @Override
  List<Website> findAll(Sort sort);

//  T find(UUID uuid);
}
