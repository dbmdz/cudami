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

  Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUUID);

  Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUUID);

  Website getWebsite(UUID rootWebpageUuid);
}
