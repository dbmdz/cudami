package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.model.api.http.exceptions.client.ResourceNotFoundException;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebpageRepositoryImpl<E extends Entity> extends EntityPartRepositoryImpl<Webpage, E>
    implements WebpageRepository<E> {

  @Autowired private WebpageRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public Webpage create() {
    return new WebpageImpl();
  }

  @Override
  public PageResponse<Webpage> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<Webpage> pageResponse =
        endpoint.find(
            f.getPageNumber(),
            f.getPageSize(),
            f.getSortField(),
            f.getSortDirection(),
            f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public Webpage findOneByIdentifier(String namespace, String id) {
    try {
      return endpoint.findOneByIdentifier(namespace, id);
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  @Override
  public Webpage findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }

  @Override
  public Webpage findOne(UUID uuid, Locale locale) {
    return endpoint.findOne(uuid, locale.toString());
  }

  @Override
  public List<Webpage> getChildren(UUID uuid) {
    return endpoint.getChildren(uuid);
  }

  @Override
  public List<Webpage> getChildren(Webpage webpage) {
    return getChildren(webpage.getUuid());
  }

  @Override
  public Webpage getParent(UUID uuid) {
    return endpoint.getParent(uuid);
  }

  @Override
  public Webpage save(Webpage identifiable) {
    return endpoint.save(identifiable);
  }

  @Override
  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUUID) {
    return endpoint.saveWithParentWebpage(webpage, parentWebpageUUID);
  }

  @Override
  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUUID) {
    return endpoint.saveWithParentWebsite(webpage, parentWebsiteUUID);
  }

  @Override
  public Webpage update(Webpage identifiable) {
    return endpoint.update(identifiable.getUuid(), identifiable);
  }
}
