package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Website persistence handling.
 *
 * @param <W> website instance
 */
public interface WebsiteRepository<W extends Website> extends EntityRepository<W> {

  List<Webpage> getRootPages(W website);

  List<Webpage> getRootPages(UUID uuid);
}
