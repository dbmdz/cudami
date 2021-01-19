package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Webpage persistence handling.
 * @param <W> instance of webpage implementation
 */
public interface WebpageRepository<W extends Webpage>
        extends NodeRepository<W>, EntityPartRepository<W> {

  W findOne(UUID uuid, Filtering filtering);

  /**
   * @param webpage newly created webpage to be saved
   * @param parentWebsiteUUID website the (root) webpage belongs to
   * @return saved webpage
   */
  W saveWithParentWebsite(W webpage, UUID parentWebsiteUUID);

  /**
   * @param webpage newly created webpage to be saved
   * @param parentWebpageUUID parent webpage the new webpage is child of
   * @return saved webpage
   */
  W saveWithParentWebpage(W webpage, UUID parentWebpageUUID);

  /**
   * @param rootWebpageUuid uuid of a webpage (webpage must be a top level webpage of the website)
   * @return the website the given root-webpage belongs to (webpage is top level webpage)
   */
  Website getWebsite(UUID rootWebpageUuid);

  default boolean updateChildrenOrder(W webpage, List<W> children) {
    if (webpage == null || children == null) {
      return false;
    }
    return updateChildrenOrder(webpage.getUuid(), children);
  }

  boolean updateChildrenOrder(UUID parentUuid, List<W> children);
}
