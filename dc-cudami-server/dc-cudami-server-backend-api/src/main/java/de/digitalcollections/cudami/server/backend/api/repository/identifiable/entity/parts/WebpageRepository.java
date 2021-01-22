package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.List;
import java.util.UUID;

/** Repository for Webpage persistence handling. */
public interface WebpageRepository extends NodeRepository<Webpage>, EntityPartRepository<Webpage> {

  /**
   * @param rootWebpageUuid uuid of a webpage (webpage must be a top level webpage of the website)
   * @return the website the given root-webpage belongs to (webpage is top level webpage)
   */
  Website getWebsite(UUID rootWebpageUuid);

  /**
   * @param webpage newly created webpage to be saved
   * @param parentWebsiteUUID website the (root) webpage belongs to
   * @return saved webpage
   */
  Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUUID);

  default boolean updateChildrenOrder(Webpage webpage, List<Webpage> children) {
    if (webpage == null || children == null) {
      return false;
    }
    return updateChildrenOrder(webpage.getUuid(), children);
  }
}
