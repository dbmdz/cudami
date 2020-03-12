package de.digitalcollections.cudami.admin.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import java.util.List;
import java.util.UUID;

/** Service for Digital Object. */
public interface DigitalObjectService extends EntityService<DigitalObject> {

  List<FileResource> getFileResources(DigitalObject digitalObject);

  List<FileResource> getFileResources(UUID digitalObjectUuid);

  List<ImageFileResource> getImageFileResources(DigitalObject digitalObject);

  List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid);

  List<FileResource> saveFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources);

  List<FileResource> saveFileResources(UUID digitalObjectUuid, List<FileResource> fileResources);
}
