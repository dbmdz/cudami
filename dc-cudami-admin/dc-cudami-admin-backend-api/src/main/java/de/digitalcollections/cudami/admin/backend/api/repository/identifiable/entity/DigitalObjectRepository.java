package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import java.util.List;
import java.util.UUID;

/** Repository for Digital Object persistence handling. */
public interface DigitalObjectRepository extends EntityRepository<DigitalObject> {

  List<FileResource> getFileResources(DigitalObject digitalObject);

  List<FileResource> getFileResources(UUID digitalObjectUuid);

  default List<ImageFileResource> getImageFileResources(DigitalObject digitalObject) {
    return getImageFileResources(digitalObject.getUuid());
  }

  List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid);

  List<FileResource> saveFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources);

  List<FileResource> saveFileResources(UUID digitalObjectUuid, List<FileResource> fileResources);
}
