package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * Repository for Digital Object persistence handling.
 */
public interface DigitalObjectRepository extends EntityRepository<DigitalObject> {

  LinkedHashSet<FileResource> getFileResources(DigitalObject digitalObject);

  LinkedHashSet<FileResource> getFileResources(UUID digitalObjectUuid);

  default LinkedHashSet<ImageFileResource> getImageFileResources(DigitalObject digitalObject) {
    return getImageFileResources(digitalObject.getUuid());
  }

  LinkedHashSet<ImageFileResource> getImageFileResources(UUID digitalObjectUuid);

  LinkedHashSet<FileResource> saveFileResources(DigitalObject digitalObject, LinkedHashSet<FileResource> fileResources);

  LinkedHashSet<FileResource> saveFileResources(UUID digitalObjectUuid, LinkedHashSet<FileResource> fileResources);
}
