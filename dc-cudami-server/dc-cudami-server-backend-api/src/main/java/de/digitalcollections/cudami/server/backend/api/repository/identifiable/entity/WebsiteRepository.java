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

  default PageResponse<Webpage> findRootWebpages(Website website, PageRequest pageRequest)
      throws RepositoryException {
    if (website == null) {
      throw new IllegalArgumentException("find failed: given website must not be null");
    }
    return findRootWebpages(website.getUuid(), pageRequest);
  }

  // FIXME: replace with pagerequest method
  List<Webpage> findRootWebpages(UUID uuid) throws RepositoryException;

  PageResponse<Webpage> findRootWebpages(UUID uuid, PageRequest pageRequest)
      throws RepositoryException;

  boolean updateRootWebpagesOrder(UUID website, List<Webpage> rootPages) throws RepositoryException;

  default boolean updateRootWebpagesOrder(Website website, List<Webpage> rootPages)
      throws RepositoryException {
    if (website == null || rootPages == null) {
      throw new IllegalArgumentException("update failed: given objects must not be null");
    }
    return updateRootWebpagesOrder(website.getUuid(), rootPages);
  }
}
