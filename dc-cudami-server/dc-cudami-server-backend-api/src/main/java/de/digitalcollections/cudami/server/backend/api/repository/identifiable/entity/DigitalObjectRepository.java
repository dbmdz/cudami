package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import java.util.List;
import java.util.UUID;

/** Repository for Digital object persistence handling. */
public interface DigitalObjectRepository extends EntityRepository<DigitalObject> {

  default List<FileResource> getFileResources(DigitalObject digitalObject) {
    return getFileResources(digitalObject.getUuid());
  }

  List<FileResource> getFileResources(UUID digitalObjectUuid);

  default List<ImageFileResource> getImageFileResources(DigitalObject digitalObject) {
    return getImageFileResources(digitalObject.getUuid());
  }

  List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid);

  List<FileResource> saveFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources);

  List<FileResource> saveFileResources(UUID digitalObjectUuid, List<FileResource> fileResources);

  DigitalObject findByIdentifier(String namespace, String id);
}
