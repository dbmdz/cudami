package de.digitalcollections.cudami.client.backend.api.repository;

import de.digitalcollections.cudami.model.api.entity.Website;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for Website persistence handling.
 * @param <T> implementation of Website interface
 */
public interface WebsiteRepository<T extends Website> extends PagingAndSortingRepository<T, Long> {

  T create();

  @Override
  List<T> findAll(Sort sort);

//  T find(UUID uuid);
}
