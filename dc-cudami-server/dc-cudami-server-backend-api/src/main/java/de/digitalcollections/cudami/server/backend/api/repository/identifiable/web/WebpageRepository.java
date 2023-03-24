package de.digitalcollections.cudami.server.backend.api.repository.identifiable.web;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.UUID;

/** Repository for Webpage persistence handling. */
public interface WebpageRepository extends NodeRepository<Webpage> {

  PageResponse<Webpage> findRootWebpagesForWebsite(UUID websiteUuid, PageRequest pageRequest);

  /**
   * @param rootWebpageUuid uuid of a webpage (webpage must be a top level webpage of the website)
   * @return the website the given root-webpage belongs to (webpage is top level webpage)
   */
  Website getWebsite(UUID rootWebpageUuid);

  /**
   * @param webpageUuid UUID of newly created webpage to be saved
   * @param parentWebsiteUUID website the (root) webpage belongs to
   * @return saved webpage
   */
  Webpage saveWithParentWebsite(UUID webpageUuid, UUID parentWebsiteUUID);
}
