package de.digitalcollections.cudami.server.backend.impl.jpa.repository;

import de.digitalcollections.cudami.server.backend.api.repository.RoleRepository;
import de.digitalcollections.cudami.server.backend.impl.jpa.entity.RoleImplJpa;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepositoryImplJpa extends AbstractPagingAndSortingRepositoryImplJpa<RoleImplJpa, Long, RoleRepositoryJpa>
        implements RoleRepository<RoleImplJpa, Long> {

  @Autowired
  @Override
  void setJpaRepository(RoleRepositoryJpa jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public RoleImplJpa create() {
    return new RoleImplJpa();
  }

  @Override
  public List<RoleImplJpa> findAll(Sort sort) {
    return jpaRepository.findAll(sort);
  }

  @Override
  public RoleImplJpa findByName(String name) {
    return jpaRepository.findByName(name);
  }

}
