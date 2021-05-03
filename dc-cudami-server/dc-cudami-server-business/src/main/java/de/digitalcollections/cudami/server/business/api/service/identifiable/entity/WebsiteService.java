package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.List;
import java.util.UUID;

/** Service for Website. */
public interface WebsiteService extends EntityService<Website> {

  SearchPageResponse<Webpage> findRootPages(UUID uuid, SearchPageRequest searchPageRequest);

  default List<Webpage> getRootPages(Website website) {
    if (website == null) {
      return null;
    }
    return getRootPages(website.getUuid());
  }

  List<Webpage> getRootPages(UUID uuid);

  default boolean updateRootPagesOrder(Website website, List<Webpage> rootPages) {
    if (website == null || rootPages == null) {
      return false;
    }
    return updateRootPagesOrder(website.getUuid(), rootPages);
  }

  boolean updateRootPagesOrder(UUID websiteUuid, List<Webpage> rootPages);
}
