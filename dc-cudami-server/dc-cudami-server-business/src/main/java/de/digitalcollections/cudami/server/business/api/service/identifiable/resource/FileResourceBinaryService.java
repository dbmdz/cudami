package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import java.io.InputStream;
import org.w3c.dom.Document;

public interface FileResourceBinaryService {

  void assertReadability(FileResource resource) throws IdentifiableServiceException;

  FileResource find(String uuid, MimeType mimeType) throws IdentifiableServiceException;

  byte[] getAsBytes(FileResource resource) throws IdentifiableServiceException;

  Document getAsDocument(FileResource resource) throws IdentifiableServiceException;

  InputStream getInputStream(FileResource resource) throws IdentifiableServiceException;

  FileResource save(FileResource fileResource, InputStream binaryData)
      throws IdentifiableServiceException;
}
