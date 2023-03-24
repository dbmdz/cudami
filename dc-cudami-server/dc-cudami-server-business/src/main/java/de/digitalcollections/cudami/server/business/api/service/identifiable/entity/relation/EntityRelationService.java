package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityToEntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface EntityRelationService {

  default void addRelation(EntityToEntityRelation relation) throws ServiceException {
    addRelation(
        relation.getSubject().getUuid(), relation.getPredicate(), relation.getObject().getUuid());
  }

  void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid)
      throws ServiceException;

  default void deleteBySubject(Entity subjectEntity) {
    deleteBySubject(subjectEntity.getUuid());
  }

  void deleteBySubject(UUID subjectEntityUuid);

  default void deleteByObject(Entity objectEntity) {
    deleteByObject(objectEntity.getUuid());
  }

  void deleteByObject(UUID objectEntityUuid);

  /**
   * Get paged, sorted, filtered relations
   *
   * @param pageRequest request param container for paging, sorting, filtering
   * @return result as paged response
   */
  PageResponse<EntityToEntityRelation> find(PageRequest pageRequest);

  default List<EntityToEntityRelation> getBySubject(Entity subjectEntity) {
    return getBySubject(subjectEntity.getUuid());
  }

  List<EntityToEntityRelation> getBySubject(UUID subjectEntityUuid);

  /**
   * Save (means create or update) a list of entity relations. This method is idempotent.
   *
   * @param entityRelations a list of entity relations to persist
   */
  void save(List<EntityToEntityRelation> entityRelations) throws ServiceException;

  void persistEntityRelations(
      Entity entity, List<EntityToEntityRelation> relations, boolean deleteExisting)
      throws ServiceException;
}
