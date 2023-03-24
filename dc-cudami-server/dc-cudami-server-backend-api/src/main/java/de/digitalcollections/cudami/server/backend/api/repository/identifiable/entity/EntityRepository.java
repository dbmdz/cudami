package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.identifiable.entity.Entity;

/**
 * @param <E> entity instance
 */
public interface EntityRepository<E extends Entity> extends IdentifiableRepository<E> {

  E getByRefId(long refId);
}
