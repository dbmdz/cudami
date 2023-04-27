package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

/** Repository for Website persistence handling. */
public interface WebsiteRepository extends EntityRepository<Website> {

  PageResponse<Webpage> findRootWebpages(UUID uuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<Webpage> findRootWebpages(Website website, PageRequest pageRequest)
      throws RepositoryException {
    if (website == null) {
      throw new IllegalArgumentException("find failed: given website must not be null");
    }
    return findRootWebpages(website.getUuid(), pageRequest);
  }

  List<Webpage> getRootWebpages(UUID uuid) throws RepositoryException;

  default List<Webpage> getRootWebpages(Website website) throws RepositoryException {
    if (website == null) {
      throw new IllegalArgumentException("get failed: given website must not be null");
    }
    return getRootWebpages(website.getUuid());
  }

  boolean updateRootWebpagesOrder(UUID website, List<Webpage> rootPages) throws RepositoryException;

  default boolean updateRootWebpagesOrder(Website website, List<Webpage> rootPages)
      throws RepositoryException {
    if (website == null || rootPages == null) {
      throw new IllegalArgumentException("update failed: given objects must not be null");
    }
    return updateRootWebpagesOrder(website.getUuid(), rootPages);
  }
}
