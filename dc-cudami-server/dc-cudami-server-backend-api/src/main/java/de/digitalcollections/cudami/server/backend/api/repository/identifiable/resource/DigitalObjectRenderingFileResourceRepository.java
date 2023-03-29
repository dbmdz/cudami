package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface DigitalObjectRenderingFileResourceRepository {

  int countDigitalObjectsForResource(UUID uuid);

  int delete(List<UUID> uuids);

  default int delete(UUID uuid) {
    return delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  default PageResponse<FileResource> findRenderingFileResources(
      DigitalObject digitalObject, PageRequest pageRequest) {
    if (digitalObject == null) {
      return null;
    }
    return findRenderingFileResources(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<FileResource> findRenderingFileResources(
      UUID digitalObjectUuid, PageRequest pageRequest);

  public int removeByDigitalObject(UUID digitalObjectUuid);

  public void saveRenderingFileResources(
      UUID digitalObjectUuid, List<FileResource> renderingResources);

  List<FileResource> getRenderingFileResources(UUID digitalObjectUuid) throws RepositoryException;
}
