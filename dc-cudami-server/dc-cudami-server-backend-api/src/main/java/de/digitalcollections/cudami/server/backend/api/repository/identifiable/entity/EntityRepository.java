package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.identifiable.entity.Entity;
import java.util.List;

/**
 * @param <E> entity instance
 */
public interface EntityRepository<E extends Entity> extends IdentifiableRepository<E> {

  E getByRefId(long refId);

  List<E> getRandom(int count);
}
