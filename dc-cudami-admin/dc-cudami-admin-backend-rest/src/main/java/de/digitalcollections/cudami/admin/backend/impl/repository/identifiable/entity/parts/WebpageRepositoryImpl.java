package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebpageRepositoryImpl<W extends Webpage, I extends Identifiable> extends IdentifiableRepositoryImpl<W> implements WebpageRepository<W, I> {

  @Autowired
  LocaleRepository localeRepository;

  @Autowired
  private WebpageRepositoryEndpoint endpoint;

  @Override
  public void addIdentifiable(UUID webpageUuid, UUID identifiableUuid) {
    endpoint.addIdentifiable(webpageUuid, identifiableUuid);
  }

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public W create() {
    return (W) new WebpageImpl();
  }

  @Override
  public PageResponse<W> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<Webpage> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
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
  public List<W> getChildren(UUID uuid) {
    return (List<W>) endpoint.getChildren(uuid);
  }

  @Override
  public List<W> getChildren(W webpage) {
    return getChildren(webpage.getUuid());
  }

  @Override
  public W saveWithParentWebsite(W webpage, UUID parentWebsiteUUID) {
    return (W) endpoint.saveWithParentWebsite(webpage, parentWebsiteUUID);
  }

  @Override
  public W saveWithParentWebpage(W webpage, UUID parentWebpageUUID) {
    return (W) endpoint.saveWithParentWebpage(webpage, parentWebpageUUID);
  }

  @Override
  public List<Identifiable> getIdentifiables(W webpage) {
    return getIdentifiables(webpage.getUuid());
  }

  private List<Identifiable> getIdentifiables(UUID uuid) {
    return endpoint.getIdentifiables(uuid);
  }

  @Override
  public List<Identifiable> saveIdentifiables(W webpage, List<Identifiable> identifiables) {
    return endpoint.saveIdentifiables(webpage.getUuid(), identifiables);
  }
}
