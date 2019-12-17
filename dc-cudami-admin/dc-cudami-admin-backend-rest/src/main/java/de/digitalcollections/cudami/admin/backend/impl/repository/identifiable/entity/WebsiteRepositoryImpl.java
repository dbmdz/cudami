package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.model.api.http.exceptions.client.ResourceNotFoundException;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebsiteRepositoryImpl extends EntityRepositoryImpl<Website>
    implements WebsiteRepository {

  @Autowired private WebsiteRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public Website create() {
    return new WebsiteImpl();
  }

  @Override
  public PageResponse<Website> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<Website> pageResponse =
        endpoint.find(
            f.getPageNumber(),
            f.getPageSize(),
            f.getSortField(),
            f.getSortDirection(),
            f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public Website findOneByIdentifier(String namespace, String id) {
    try {
      return endpoint.findOneByIdentifier(namespace, id);
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  @Override
  public Website findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }

  @Override
  public Website findOne(UUID uuid, Locale locale) {
    return endpoint.findOne(uuid, locale.toString());
  }

  @Override
  public List<Webpage> getRootPages(UUID uuid) {
    return endpoint.getRootPages(uuid);
  }

  @Override
  public List<Webpage> getRootPages(Website website) {
    return getRootPages(website.getUuid());
  }

  @Override
  public Website save(Website website) {
    return endpoint.save(website);
  }

  @Override
  public Website update(Website website) {
    return endpoint.update(website.getUuid(), website);
  }
}
