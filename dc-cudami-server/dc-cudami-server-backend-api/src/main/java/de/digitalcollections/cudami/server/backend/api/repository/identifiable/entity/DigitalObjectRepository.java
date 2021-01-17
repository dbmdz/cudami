package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Digital object persistence handling.
 *
 * @param <D> instance implementing DigitalObject
 */
public interface DigitalObjectRepository<D extends DigitalObject> extends EntityRepository<D> {

  default PageResponse<Collection> getCollections(D digitalObject, PageRequest pageRequest) {
    return getCollections(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<Collection> getCollections(UUID digitalObjectUuid, PageRequest pageRequest);

  default List<FileResource> getFileResources(D digitalObject) {
    return getFileResources(digitalObject.getUuid());
  }

  List<FileResource> getFileResources(UUID digitalObjectUuid);

  default List<ImageFileResource> getImageFileResources(D digitalObject) {
    return getImageFileResources(digitalObject.getUuid());
  }

  List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid);

  default PageResponse<Project> getProjects(D digitalObject, PageRequest pageRequest) {
    return getProjects(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<Project> getProjects(UUID digitalObjectUuid, PageRequest pageRequest);

  List<FileResource> saveFileResources(D digitalObject, List<FileResource> fileResources);

  List<FileResource> saveFileResources(UUID digitalObjectUuid, List<FileResource> fileResources);

  void deleteFileResources(UUID digitalObjectUuid);
}
