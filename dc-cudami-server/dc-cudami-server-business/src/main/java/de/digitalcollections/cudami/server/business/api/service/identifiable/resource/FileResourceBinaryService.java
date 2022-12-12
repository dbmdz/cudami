package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.io.InputStream;
import org.w3c.dom.Document;

public interface FileResourceBinaryService {

  void assertReadability(FileResource resource) throws IdentifiableServiceException;

  FileResource find(String uuid, MimeType mimeType) throws IdentifiableServiceException;

  byte[] getAsBytes(FileResource resource) throws IdentifiableServiceException;

  Document getAsDocument(FileResource resource) throws IdentifiableServiceException;

  InputStream getInputStream(FileResource resource) throws IdentifiableServiceException;

  void save(FileResource fileResource, InputStream binaryData)
      throws ValidationException, ServiceException;
}
