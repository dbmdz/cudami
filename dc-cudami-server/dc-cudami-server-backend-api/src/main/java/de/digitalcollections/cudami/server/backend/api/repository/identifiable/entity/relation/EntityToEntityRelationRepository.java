package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation;

import de.digitalcollections.cudami.server.backend.api.repository.PagingSortingFilteringRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityToEntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface EntityToEntityRelationRepository
    extends PagingSortingFilteringRepository<EntityToEntityRelation> {

  default void addRelation(EntityToEntityRelation relation) throws RepositoryException {
    addRelation(
        relation.getSubject().getUuid(), relation.getPredicate(), relation.getObject().getUuid());
  }

  void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid)
      throws RepositoryException;

  default void deleteByObject(Entity objectEntity) throws RepositoryException {
    if (objectEntity == null) {
      throw new IllegalArgumentException("object entity must not be null");
    }
    deleteByObject(objectEntity.getUuid());
  }

  void deleteByObject(UUID objectEntityUuid) throws RepositoryException;

  default void deleteBySubject(Entity subjectEntity) throws RepositoryException {
    deleteBySubject(subjectEntity.getUuid());
  }

  void deleteBySubject(UUID subjectEntityUuid) throws RepositoryException;

  default PageResponse<EntityToEntityRelation> findBySubject(
      Entity subjectEntity, PageRequest pageRequest) throws RepositoryException {
    if (subjectEntity == null) {
      throw new IllegalArgumentException("subject entity must not be null");
    }
    return findBySubject(subjectEntity.getUuid(), pageRequest);
  }

  PageResponse<EntityToEntityRelation> findBySubject(
      UUID subjectEntityUuid, PageRequest pageRequest) throws RepositoryException;

  default void save(EntityToEntityRelation relation) throws RepositoryException {
    save(List.of(relation));
  }

  /**
   * Persists a list of EntityRelations
   *
   * @param entityRelations list of entity-predicate-entity relations to be persisted
   * @throws RepositoryException in case of an error, e.g. a referenced predicate does not yet exist
   */
  void save(List<EntityToEntityRelation> entityRelations) throws RepositoryException;

  void save(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid)
      throws RepositoryException;
}
