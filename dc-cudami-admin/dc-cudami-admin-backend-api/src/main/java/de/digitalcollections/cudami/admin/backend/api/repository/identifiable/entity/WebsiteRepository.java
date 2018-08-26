package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.List;

/**
 * Repository for Website persistence handling.
 *
 * @param <W> website instance
 */
public interface WebsiteRepository<W extends Website> extends EntityRepository<W> {

  List<Webpage> getRootPages(W website);
}
