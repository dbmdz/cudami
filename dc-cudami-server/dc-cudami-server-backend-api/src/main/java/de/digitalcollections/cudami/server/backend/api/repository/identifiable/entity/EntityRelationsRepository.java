package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import java.util.List;

public interface EntityRelationsRepository {

  /**
   * Persists a list of EntityRelations
   *
   * @param entityRelations list of EntityRelations to be persisted
   * @return list of persisted EntityRelations
   */
  List<EntityRelation> save(List<EntityRelation> entityRelations);
}
