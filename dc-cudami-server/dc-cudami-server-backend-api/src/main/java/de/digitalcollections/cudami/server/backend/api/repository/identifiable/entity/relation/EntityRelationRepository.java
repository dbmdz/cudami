package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation;

import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface EntityRelationRepository {

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

  default void save(EntityRelation relation) {
    save(relation.getSubject().getUuid(), relation.getPredicate(), relation.getObject().getUuid());
  }

  void save(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid);

  /**
   * Persists a list of EntityRelations
   *
   * @param entityRelations list of entity-predicate-entity relations to be persisted
   * @return list of persisted EntityRelations
   */
  List<EntityRelation> save(List<EntityRelation> entityRelations);
}
