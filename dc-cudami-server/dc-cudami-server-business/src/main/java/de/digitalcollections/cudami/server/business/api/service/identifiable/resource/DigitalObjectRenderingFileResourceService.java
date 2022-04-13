package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;

public interface DigitalObjectRenderingFileResourceService
    extends FileResourceMetadataService<FileResource> {

  default List<FileResource> saveForDigitalObject(
      DigitalObject digitalObject, List<FileResource> renderingResources)
      throws CudamiServiceException {
    if (digitalObject == null) {
      throw new CudamiServiceException("DigitalObject must not be null");
    }
    if (renderingResources == null) {
      return null;
    }
    return saveForDigitalObject(digitalObject.getUuid(), renderingResources);
  }

  List<FileResource> saveForDigitalObject(
      UUID digitalObjectUuid, List<FileResource> renderingResources);

  default List<FileResource> getForDigitalObject(DigitalObject digitalObject)
      throws CudamiServiceException {
    if (digitalObject == null) {
      throw new CudamiServiceException("DigitalObject must not be null");
    }
    return getForDigitalObject(digitalObject.getUuid());
  }

  List<FileResource> getForDigitalObject(UUID digitalObjectUuid);
}
