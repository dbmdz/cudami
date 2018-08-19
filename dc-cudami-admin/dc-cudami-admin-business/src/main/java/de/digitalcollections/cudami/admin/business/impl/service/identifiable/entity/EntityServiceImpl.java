package de.digitalcollections.cudami.admin.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.admin.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
//@Transactional(readOnly = true)
public class EntityServiceImpl<E extends Entity> extends IdentifiableServiceImpl<E> implements EntityService<E> {

  @Autowired
  public EntityServiceImpl(@Qualifier("entityRepositoryImpl")  EntityRepository<E> repository) {
    super(repository);
  }
}
