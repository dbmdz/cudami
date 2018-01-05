package de.digitalcollections.cudami.client.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import java.util.List;

/**
 * Repository for Website persistence handling.
 *
 * @param <W> entity instance
 */
public interface WebsiteRepository<W extends Website> extends EntityRepository<W> {

  List<Webpage> getRootNodes(W website);
}
