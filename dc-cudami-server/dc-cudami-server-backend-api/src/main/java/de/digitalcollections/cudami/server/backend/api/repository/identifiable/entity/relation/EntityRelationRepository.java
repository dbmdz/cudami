package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface EntityRelationRepository {

  default void addRelation(EntityRelation relation) throws RepositoryException {
    addRelation(
        relation.getSubject().getUuid(), relation.getPredicate(), relation.getObject().getUuid());
  }

  void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid)
      throws RepositoryException;

  default void deleteByObject(Entity objectEntity) {
    deleteByObject(objectEntity.getUuid());
  }

  void deleteByObject(UUID objectEntityUuid);

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

  default PageResponse<EntityRelation> findBySubject(
      Entity subjectEntity, PageRequest pageRequest) {
    if (subjectEntity == null) {
      return null;
    }
    return findBySubject(subjectEntity.getUuid(), pageRequest);
  }

  PageResponse<EntityRelation> findBySubject(UUID subjectEntityUuid, PageRequest pageRequest);

  default void save(EntityRelation relation) throws RepositoryException {
    save(List.of(relation));
  }

  /**
   * Persists a list of EntityRelations
   *
   * @param entityRelations list of entity-predicate-entity relations to be persisted
   * @throws RepositoryException in case of an error, e.g. a referenced predicate does not yet exist
   */
  void save(List<EntityRelation> entityRelations) throws RepositoryException;

  void save(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid)
      throws RepositoryException;
}
