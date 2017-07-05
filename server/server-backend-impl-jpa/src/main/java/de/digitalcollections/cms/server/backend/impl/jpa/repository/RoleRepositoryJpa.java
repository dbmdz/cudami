package de.digitalcollections.cms.server.backend.impl.jpa.repository;

import de.digitalcollections.cms.server.backend.impl.jpa.entity.RoleImplJpa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepositoryJpa extends JpaRepository<RoleImplJpa, Long> {

  public RoleImplJpa findByName(String name);

}
