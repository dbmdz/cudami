package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import java.util.UUID;

/**
 * Repository for Webpage persistence handling.
 *
 * @param <W> webpage instance
 */
public interface WebpageRepository<W extends Webpage> extends ResourceRepository<W>, NodeRepository<W> {

  W saveWithParentWebsite(W webpage, UUID parentWebsiteUuid);

  W saveWithParentWebpage(W webpage, UUID parentWebpageUuid);
}
