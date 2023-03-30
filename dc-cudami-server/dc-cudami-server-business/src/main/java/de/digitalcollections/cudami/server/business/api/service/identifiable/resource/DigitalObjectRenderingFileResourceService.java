package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.util.List;

public interface DigitalObjectRenderingFileResourceService {

  void deleteRenderingFileResources(DigitalObject digitalObject) throws ServiceException;

  List<FileResource> getRenderingFileResources(DigitalObject digitalObject);

  List<FileResource> setRenderingFileResources(
      DigitalObject digitalObject, List<FileResource> renderingResources) throws ServiceException;
}
