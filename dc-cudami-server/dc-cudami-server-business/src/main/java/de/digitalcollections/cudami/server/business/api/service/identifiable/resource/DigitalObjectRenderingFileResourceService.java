package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;

public interface DigitalObjectRenderingFileResourceService {

  default List<FileResource> setRenderingFileResources(
      DigitalObject digitalObject, List<FileResource> renderingResources) throws ServiceException {
    if (digitalObject == null) {
      throw new ServiceException("DigitalObject must not be null");
    }
    if (renderingResources == null) {
      return null;
    }
    return setRenderingFileResources(digitalObject.getUuid(), renderingResources);
  }

  List<FileResource> setRenderingFileResources(
      UUID digitalObjectUuid, List<FileResource> renderingResources) throws ServiceException;

  default List<FileResource> getRenderingFileResources(DigitalObject digitalObject)
      throws ServiceException {
    if (digitalObject == null) {
      throw new ServiceException("DigitalObject must not be null");
    }
    return getRenderingFileResources(digitalObject.getUuid());
  }

  List<FileResource> getRenderingFileResources(UUID digitalObjectUuid);

  void deleteRenderingFileResources(UUID digitalObjectUuid) throws ServiceException;
}
