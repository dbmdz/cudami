package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

/** Repository for CorporateBody persistence handling. */
public interface CorporateBodyRepository extends AgentRepository<CorporateBody> {

  default List<CorporateBody> findCollectionRelatedCorporateBodies(
      Collection collection, Filtering filtering) throws RepositoryException {
    if (collection == null) {
      throw new IllegalArgumentException("find failed: given collection must not be null");
    }
    return findCollectionRelatedCorporateBodies(collection.getUuid(), filtering);
  }

  // FIXME: remove it, just use PageRequest
  List<CorporateBody> findCollectionRelatedCorporateBodies(UUID collectionUuid, Filtering filtering)
      throws RepositoryException;

  PageResponse<CorporateBody> findCollectionRelatedCorporateBodies(
      UUID collectionUuid, PageRequest pageRequest) throws RepositoryException;
}
