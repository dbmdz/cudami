package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

/** Repository for LinkedDataFileResource persistence handling. */
public interface DigitalObjectLinkedDataFileResourceRepository {

  int countDigitalObjectsForResource(UUID uuid);

  int delete(List<UUID> uuids);

  default int delete(UUID uuid) {
    return delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  default PageResponse<LinkedDataFileResource> findLinkedDataFileResources(
      DigitalObject digitalObject, PageRequest pageRequest) {
    if (digitalObject == null) {
      return null;
    }
    return findLinkedDataFileResources(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<LinkedDataFileResource> findLinkedDataFileResources(
      UUID digitalObjectUuid, PageRequest pageRequest);

  List<LinkedDataFileResource> getLinkedDataFileResources(UUID digitalObjectUuid)
      throws RepositoryException;

  List<LinkedDataFileResource> setLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources)
      throws RepositoryException;
}
