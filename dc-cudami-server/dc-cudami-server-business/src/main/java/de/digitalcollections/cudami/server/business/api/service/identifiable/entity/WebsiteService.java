package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Service for Website. */
public interface WebsiteService extends EntityService<Website> {

  List<Locale> getLanguages();

  default List<Webpage> getRootPages(Website website) {
    if (website == null) {
      return null;
    }
    return getRootPages(website.getUuid());
  }

  List<Webpage> getRootPages(UUID uuid);

  PageResponse<Webpage> getRootPages(UUID uuid, PageRequest pageRequest);

  default boolean updateRootPagesOrder(Website website, List<Webpage> rootPages) {
    if (website == null || rootPages == null) {
      return false;
    }
    return updateRootPagesOrder(website.getUuid(), rootPages);
  }

  boolean updateRootPagesOrder(UUID websiteUuid, List<Webpage> rootPages);
}
