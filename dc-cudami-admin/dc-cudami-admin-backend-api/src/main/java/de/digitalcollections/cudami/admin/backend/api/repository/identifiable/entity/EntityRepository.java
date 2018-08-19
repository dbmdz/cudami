package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;

/**
 * @param <E> entity instance
 */
public interface EntityRepository<E extends Entity> extends IdentifiableRepository<E> {

}
