package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiablesContainerRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.UUID;

/**
 * Repository for Webpage persistence handling.
 *
 * @param <W> webpage instance
 */
public interface WebpageRepository<W extends Webpage> extends NodeRepository<W> {

  W saveWithParentWebsite(W webpage, UUID parentWebsiteUuid);

  W saveWithParentWebpage(W webpage, UUID parentWebpageUuid);
}
