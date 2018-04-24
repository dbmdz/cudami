package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import java.util.UUID;

/**
 * Repository for Webpage persistence handling.
 *
 * @param <W> entity instance
 */
public interface WebpageRepository<W extends Webpage> extends IdentifiableRepository<W> {

  Webpage save(W webpage, UUID websiteUuid);
}
