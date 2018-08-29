package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
// FIXME: duplicate methods (replace by functional call with specific endpoint instance?)
public class WebsiteRepositoryImpl<W extends WebsiteImpl> extends EntityRepositoryImpl<W> implements WebsiteRepository<W> {

  @Autowired
  private WebsiteRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public W create() {
    return (W) new WebsiteImpl();
  }

  @Override
  public PageResponse<W> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<Website> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public W findOne(UUID uuid) {
    return (W) endpoint.findOne(uuid);
  }

  @Override
  public W save(W identifiable) {
    return (W) endpoint.save(identifiable);
  }

  @Override
  public W update(W identifiable) {
    return (W) endpoint.update(identifiable.getUuid(), identifiable);
  }

  @Override
  public List<Webpage> getRootPages(W website) {
    return (List<Webpage>) endpoint.getRootPages(website.getUuid());
  }
}
