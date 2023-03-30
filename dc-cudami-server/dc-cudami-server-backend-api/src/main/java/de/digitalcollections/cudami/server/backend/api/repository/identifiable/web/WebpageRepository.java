package de.digitalcollections.cudami.server.backend.api.repository.identifiable.web;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.UUID;

/** Repository for Webpage persistence handling. */
public interface WebpageRepository extends NodeRepository<Webpage> {

  default PageResponse<Webpage> findRootWebpagesForWebsite(Website website, PageRequest pageRequest)
      throws RepositoryException {
    if (website == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findRootWebpagesForWebsite(website.getUuid(), pageRequest);
  }

  PageResponse<Webpage> findRootWebpagesForWebsite(UUID websiteUuid, PageRequest pageRequest)
      throws RepositoryException;

  default Website getWebsite(Webpage webpage) throws RepositoryException {
    if (webpage == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getWebsite(webpage.getUuid());
  }

  /**
   * @param rootWebpageUuid uuid of a webpage (webpage must be a top level webpage of the website)
   * @return the website the given root-webpage belongs to (webpage is top level webpage)
   */
  Website getWebsite(UUID rootWebpageUuid) throws RepositoryException;

  default Webpage saveWithParentWebsite(Webpage webpage, Website website)
      throws RepositoryException {
    if (webpage == null || website == null) {
      throw new IllegalArgumentException("save failed: given objects must not be null");
    }
    return saveWithParentWebsite(webpage.getUuid(), website.getUuid());
  }

  /**
   * @param webpageUuid UUID of newly created webpage to be saved
   * @param parentWebsiteUUID website the (root) webpage belongs to
   * @return saved webpage
   * @throws RepositoryException
   */
  Webpage saveWithParentWebsite(UUID webpageUuid, UUID parentWebsiteUUID)
      throws RepositoryException;
}
