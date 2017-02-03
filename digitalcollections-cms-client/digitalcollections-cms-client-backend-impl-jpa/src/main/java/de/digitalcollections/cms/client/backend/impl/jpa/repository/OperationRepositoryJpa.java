package de.digitalcollections.cms.client.backend.impl.jpa.repository;

import de.digitalcollections.cms.client.backend.impl.jpa.entity.OperationImplJpa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepositoryJpa extends JpaRepository<OperationImplJpa, Long> {

  public OperationImplJpa findByName(String name);

}
