package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifiablesContainerRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.UUID;

/**
 * Repository for Webpage persistence handling.
 *
 * @param <W> webpage instance
 * @param <I> identifiable instance
 */
public interface WebpageRepository<W extends Webpage, I extends Identifiable> extends NodeRepository<W>, IdentifiablesContainerRepository<W, I> {

  W saveWithParentWebsite(W webpage, UUID parentWebsiteUUID);

  W saveWithParentWebpage(W webpage, UUID parentWebpageUUID);
}
