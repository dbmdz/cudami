package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.UUID;

/**
 * Repository for Webpage persistence handling.
 *
 * @param <E> entity type
 */
public interface WebpageRepository<E extends Entity>
    extends NodeRepository<Webpage>, EntityPartRepository<Webpage, E> {

  /**
   * @param webpage newly created webpage to be saved
   * @param parentWebsiteUUID website the (root) webpage belongs to
   * @return saved webpage
   */
  Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUUID);

  /**
   * @param webpage newly created webpage to be saved
   * @param parentWebpageUUID parent webpage the new webpage is child of
   * @return saved webpage
   */
  Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUUID);

  /**
   * @param rootWebpageUuid uuid of a webpage (webpage must be a top level webpage of the website)
   * @return the website the given root-webpage belongs to (webpage is top level webpage)
   */
  Website getWebsite(UUID rootWebpageUuid);
}
