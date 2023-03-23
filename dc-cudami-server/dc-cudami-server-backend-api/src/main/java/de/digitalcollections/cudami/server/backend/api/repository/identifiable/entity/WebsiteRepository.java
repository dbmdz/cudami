package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

/** Repository for Website persistence handling. */
public interface WebsiteRepository extends EntityRepository<Website> {

  default PageResponse<Webpage> findRootPages(Website website, PageRequest pageRequest) {
    if (website == null) {
      return null;
    }
    return findRootWebpages(website.getUuid(), pageRequest);
  }

  PageResponse<Webpage> findRootWebpages(UUID uuid, PageRequest pageRequest);

  default boolean updateRootWebpagesOrder(Website website, List<Webpage> rootPages) {
    if (website == null || rootPages == null) {
      return false;
    }
    return updateRootWebpagesOrder(website.getUuid(), rootPages);
  }

  boolean updateRootWebpagesOrder(UUID website, List<Webpage> rootPages);
}
