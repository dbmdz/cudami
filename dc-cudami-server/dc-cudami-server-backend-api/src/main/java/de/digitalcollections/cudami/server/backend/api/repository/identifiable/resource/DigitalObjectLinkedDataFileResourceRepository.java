package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.UUID;

/** Repository for LinkedDataFileResource persistence handling. */
public interface DigitalObjectLinkedDataFileResourceRepository {

  int countDigitalObjectsForResource(UUID uuid) throws RepositoryException;

  int delete(List<UUID> uuids) throws RepositoryException;

  default int delete(UUID uuid) throws RepositoryException {
    return delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  default PageResponse<LinkedDataFileResource> findLinkedDataFileResources(
      DigitalObject digitalObject, PageRequest pageRequest) throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findLinkedDataFileResources(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<LinkedDataFileResource> findLinkedDataFileResources(
      UUID digitalObjectUuid, PageRequest pageRequest) throws RepositoryException;

  default List<LinkedDataFileResource> getLinkedDataFileResources(DigitalObject digitalObject)
      throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getLinkedDataFileResources(digitalObject.getUuid());
  }

  List<LinkedDataFileResource> getLinkedDataFileResources(UUID digitalObjectUuid)
      throws RepositoryException;

  default void setLinkedDataFileResources(
      DigitalObject digitalObject, List<LinkedDataFileResource> linkedDataFileResources)
      throws RepositoryException, ValidationException {
    if (digitalObject == null || linkedDataFileResources == null) {
      throw new IllegalArgumentException("set failed: given objects must not be null");
    }
    setLinkedDataFileResources(digitalObject.getUuid(), linkedDataFileResources);
  }

  void setLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources)
      throws RepositoryException, ValidationException;
}
