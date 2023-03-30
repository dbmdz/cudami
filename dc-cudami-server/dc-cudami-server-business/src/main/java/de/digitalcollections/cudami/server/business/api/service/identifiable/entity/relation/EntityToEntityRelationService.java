package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;

// TODO: really own service? maybe move to EntityService? deleteByObject ->
// deleteEntityRelationsByObject(Entity)?
public interface EntityToEntityRelationService {

  void addRelation(EntityRelation relation) throws ServiceException;

  void deleteByObject(Entity objectEntity) throws ServiceException;

  void deleteBySubject(Entity subjectEntity) throws ServiceException;

  /**
   * Get paged, sorted, filtered relations
   *
   * @param pageRequest request param container for paging, sorting, filtering
   * @return result as paged response
   */
  PageResponse<EntityRelation> find(PageRequest pageRequest) throws ServiceException;

  PageResponse<EntityRelation> findBySubject(Entity subjectEntity, PageRequest pageRequest)
      throws ServiceException;

  /**
   * Save (means create or update) a list of entity relations. This method is idempotent.
   *
   * @param entityRelations a list of entity relations to persist
   */
  void save(List<EntityRelation> entityRelations) throws ServiceException;

  void setEntityRelations(
      Entity objectEntity, List<EntityRelation> relations, boolean deleteExisting)
      throws ServiceException;
}
