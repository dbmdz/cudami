package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import java.util.List;
import java.util.UUID;

/** Repository for Website persistence handling. */
public interface WebsiteRepository extends EntityRepository<Website> {

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

  boolean updateRootPagesOrder(UUID website, List<Webpage> rootPages);
}
