package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;

public interface DigitalObjectRenderingFileResourceService
    extends FileResourceMetadataService<FileResource> {

  default List<FileResource> saveForDigitalObject(
      DigitalObject digitalObject, List<FileResource> renderingResources) {
    if (digitalObject == null) {
      // TODO Better throw an exception
      return null;
    }
    if (renderingResources == null) {
      return null;
    }
    return saveForDigitalObject(digitalObject.getUuid(), renderingResources);
  }

  List<FileResource> saveForDigitalObject(
      UUID digitalObjectUuid, List<FileResource> renderingResources);

  default List<FileResource> getForDigitalObject(DigitalObject digitalObject) {
    if (digitalObject == null) {
      // TODO Better throw an exception
      return null;
    }
    return getForDigitalObject(digitalObject.getUuid());
  }

  List<FileResource> getForDigitalObject(UUID digitalObjectUuid);
}
