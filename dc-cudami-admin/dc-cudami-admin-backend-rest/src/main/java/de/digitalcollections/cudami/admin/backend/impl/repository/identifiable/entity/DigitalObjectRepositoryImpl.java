package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
// FIXME: duplicate methods (replace by functional call with specific endpoint instance?)
public class DigitalObjectRepositoryImpl<D extends DigitalObject> extends EntityRepositoryImpl<D> implements DigitalObjectRepository<D> {

  @Autowired
  private DigitalObjectRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public D create() {
    return (D) new DigitalObjectImpl();
  }

  @Override
  public PageResponse<D> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<DigitalObject> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public D findOne(UUID uuid) {
    return (D) endpoint.findOne(uuid);
  }

  @Override
  public D save(D identifiable) {
    return (D) endpoint.save(identifiable);
  }

  @Override
  public D update(D identifiable) {
    return (D) endpoint.update(identifiable.getUuid(), identifiable);
  }
}
