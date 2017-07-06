package de.digitalcollections.cudami.client.backend.api.repository;

import de.digitalcollections.cudami.model.api.security.Operation;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for Operation persistence handling.
 * @param <T> implementation of Operation interface
 */
public interface OperationRepository<T extends Operation> extends PagingAndSortingRepository<T, Long> {

  T create();

  @Override
  List<T> findAll(Sort sort);

  T findByName(String name);
}
