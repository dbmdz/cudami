package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.identifiable.entity.Entity;
import java.util.List;
import java.util.UUID;

/**
 * @param <E> entity instance
 */
public interface EntityRepository<E extends Entity> extends IdentifiableRepository<E> {

  default List<Entity> setRelatedEntities(UUID identifiableUuid, List<Entity> entities) {
    throw new UnsupportedOperationException(
        "Not supported: As relations between Entities need a predicate use EntityToEntityRelationRepository.");
  }

  E getByRefId(long refId);
}
