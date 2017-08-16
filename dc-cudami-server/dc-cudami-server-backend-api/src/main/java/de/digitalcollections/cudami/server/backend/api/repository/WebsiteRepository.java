package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.cudami.model.api.entity.Website;
import java.io.Serializable;

/**
 * Repository for Website persistence handling.
 *
 * @param <W> entity instance
 * @param <ID> unique id
 */
public interface WebsiteRepository<W extends Website, ID extends Serializable> extends EntityRepository<W, ID> {
}
