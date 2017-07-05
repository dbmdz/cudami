package de.digitalcollections.cms.client.backend.api.repository;

import de.digitalcollections.cms.model.api.security.Operation;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for Operation persistence handling.
 */
public interface OperationRepository extends PagingAndSortingRepository<Operation, Long> {

  Operation create();

  @Override
  List<Operation> findAll(Sort sort);

  Operation findByName(String name);
}
