package de.digitalcollections.cudami.client.backend.api.repository.entity;

import de.digitalcollections.cudami.model.api.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.Webpage;
import java.util.List;

/**
 * Repository for Website persistence handling.
 *
 * @param <W> entity instance
 */
public interface WebsiteRepository<W extends Website> extends EntityRepository<W> {

  List<Webpage> getRootNodes(W website);
}
