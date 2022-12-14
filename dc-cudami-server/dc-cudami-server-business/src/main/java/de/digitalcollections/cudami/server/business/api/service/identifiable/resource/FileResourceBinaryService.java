package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.io.InputStream;
import org.w3c.dom.Document;

public interface FileResourceBinaryService {

  void assertReadability(FileResource resource) throws ServiceException;

  FileResource find(String uuid, MimeType mimeType) throws ServiceException;

  byte[] getAsBytes(FileResource resource) throws ServiceException;

  Document getAsDocument(FileResource resource) throws ServiceException;

  InputStream getInputStream(FileResource resource) throws ServiceException;

  void save(FileResource fileResource, InputStream binaryData)
      throws ValidationException, ServiceException;
}
