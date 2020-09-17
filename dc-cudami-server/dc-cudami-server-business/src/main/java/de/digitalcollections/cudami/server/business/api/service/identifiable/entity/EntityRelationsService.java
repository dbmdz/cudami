package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import java.util.List;

public interface EntityRelationsService {

  /**
   * Save (means create or update) a list of entity relations. This method is idempotent.
   *
   * @param entityRelations a list of entity relations to persist
   * @return the persisted list of entity relations
   */
  List<EntityRelation> saveEntityRelations(List<EntityRelation> entityRelations);
}
