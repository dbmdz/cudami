package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface EntityRelationsRepository {

  /**
   * Get paged, sorted, filtered relations
   *
   * @param pageRequest request param container for paging, sorting, filtering
   * @return result as paged response
   */
  PageResponse<EntityRelation> find(PageRequest pageRequest);

  /**
   * Persists a list of EntityRelations
   *
   * @param entityRelations list of EntityRelations to be persisted
   * @return list of persisted EntityRelations
   */
  List<EntityRelation> save(List<EntityRelation> entityRelations);

  /**
   * Deletes all EntityRelations for a given (subject,predicate) pair
   *
   * @param subjectUuid the uuid of the subject
   * @param predicate the predicate
   * @return flag for successful execution
   */
  boolean deleteAllForSubjectAndPredicate(UUID subjectUuid, String predicate);
}
