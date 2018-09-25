package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.*;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EntityRepositoryImpl<E extends Entity> extends IdentifiableRepositoryImpl<E> implements EntityRepository<E> {

  @Autowired
  private EntityRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public E create() {
    return (E) new EntityImpl();
  }

  @Override
  public PageResponse<E> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<Entity> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public E findOne(UUID uuid) {
    return (E) endpoint.findOne(uuid);
  }

  @Override
  public E save(E identifiable) {
    return (E) endpoint.save(identifiable);
  }

  @Override
  public E update(E identifiable) {
    return (E) endpoint.update(identifiable.getUuid(), identifiable);
  }
}
