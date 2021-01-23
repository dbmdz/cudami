package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;

/** Service for Digital Object. */
public interface DigitalObjectService extends EntityService<DigitalObject> {

  void deleteFileResources(UUID digitalObjectUuid);

  PageResponse<Collection> getActiveCollections(
      DigitalObject digitalObject, PageRequest pageRequest);

  default PageResponse<Collection> getCollections(
      DigitalObject digitalObject, PageRequest pageRequest) {
    return getCollections(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<Collection> getCollections(UUID digitalObjectUuid, PageRequest pageRequest);

  default List<FileResource> getFileResources(DigitalObject digitalObject) {
    return getFileResources(digitalObject.getUuid());
  }

  List<FileResource> getFileResources(UUID digitalObjectUuid);

  default List<ImageFileResource> getImageFileResources(DigitalObject digitalObject) {
    return getImageFileResources(digitalObject.getUuid());
  }

  List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid);

  default Item getItem(DigitalObject digitalObject) {
    if (digitalObject == null) {
      return null;
    }
    return getItem(digitalObject.getUuid());
  }

  Item getItem(UUID digitalObjectUuid);

  default PageResponse<Project> getProjects(DigitalObject digitalObject, PageRequest pageRequest) {
    return getProjects(digitalObject.getUuid(), pageRequest);
  }

  PageResponse<Project> getProjects(UUID digitalObjectUuid, PageRequest pageRequest);

  default List<FileResource> saveFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources) {
    if (fileResources == null) {
      return null;
    }
    return saveFileResources(digitalObject.getUuid(), fileResources);
  }

  List<FileResource> saveFileResources(UUID digitalObjectUuid, List<FileResource> fileResources);
}
