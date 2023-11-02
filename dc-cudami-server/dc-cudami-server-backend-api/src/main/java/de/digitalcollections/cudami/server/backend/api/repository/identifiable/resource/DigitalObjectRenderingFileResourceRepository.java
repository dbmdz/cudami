package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public interface DigitalObjectRenderingFileResourceRepository {

  int countDigitalObjectsForResource(UUID uuid) throws RepositoryException;

  default int delete(FileResource fileResource) throws RepositoryException {
    return delete(Set.of(fileResource)); // same performance as "where uuid = :uuid"
  }

  default int delete(Set<FileResource> fileResources) throws RepositoryException {
    List<UUID> list = fileResources.stream().map(i -> i.getUuid()).collect(Collectors.toList());
    return delete(list);
  }

  int delete(List<UUID> uuids) throws RepositoryException;

  default int delete(UUID uuid) throws RepositoryException {
    return delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  default PageResponse<FileResource> findRenderingFileResources(
      DigitalObject digitalObject, PageRequest pageRequest) throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findRenderingFileResources(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<FileResource> findRenderingFileResources(
      UUID digitalObjectUuid, PageRequest pageRequest) throws RepositoryException;

  default List<FileResource> getRenderingFileResources(DigitalObject digitalObject)
      throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getRenderingFileResources(digitalObject.getUuid());
  }

  List<FileResource> getRenderingFileResources(UUID digitalObjectUuid) throws RepositoryException;

  default int removeByDigitalObject(DigitalObject digitalObject) throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("remove failed: given object must not be null");
    }
    return removeByDigitalObject(digitalObject.getUuid());
  }

  public int removeByDigitalObject(UUID digitalObjectUuid) throws RepositoryException;

  default void setRenderingFileResources(
      DigitalObject digitalObject, List<FileResource> renderingResources)
      throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("set failed: given object must not be null");
    }
    if (renderingResources == null) {
      return;
    }
    setRenderingFileResources(digitalObject.getUuid(), renderingResources);
  }

  public void setRenderingFileResources(
      UUID digitalObjectUuid, List<FileResource> renderingResources) throws RepositoryException;
}
