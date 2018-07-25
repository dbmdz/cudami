package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import java.util.List;

/**
 * Repository for Website persistence handling.
 *
 * @param <W> entity instance
 */
public interface WebsiteRepository<W extends Website> extends IdentifiableRepository<W> {

  List<Webpage> getRootPages(W website);
}
