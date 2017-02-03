package de.digitalcollections.cms.client.backend.impl.jpa.repository;

import de.digitalcollections.cms.client.backend.impl.jpa.entity.RoleImplJpa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepositoryJpa extends JpaRepository<RoleImplJpa, Long> {

  public RoleImplJpa findByName(String name);

}
