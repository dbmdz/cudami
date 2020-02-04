package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.io.InputStream;

public interface FileResourceBinaryService {

  FileResource save(FileResource fileResource, InputStream binaryData)
      throws IdentifiableServiceException;
}
