package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation;

import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface EntityRelationService {

  default void addRelation(EntityRelation relation) {
    addRelation(
        relation.getSubject().getUuid(), relation.getPredicate(), relation.getObject().getUuid());
  }

  void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid);

  default void deleteBySubject(Entity subjectEntity) {
    deleteBySubject(subjectEntity.getUuid());
  }

  void deleteBySubject(UUID subjectEntityUuid);

  /**
   * Get paged, sorted, filtered relations
   *
   * @param pageRequest request param container for paging, sorting, filtering
   * @return result as paged response
   */
  PageResponse<EntityRelation> find(PageRequest pageRequest);

  default List<EntityRelation> getBySubject(Entity subjectEntity) {
    return getBySubject(subjectEntity.getUuid());
  }

  List<EntityRelation> getBySubject(UUID subjectEntityUuid);

  /**
   * Save (means create or update) a list of entity relations. This method is idempotent.
   *
   * @param entityRelations a list of entity relations to persist
   * @return the persisted list of entity relations
   */
  List<EntityRelation> save(List<EntityRelation> entityRelations);
}
