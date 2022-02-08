package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.w3c.dom.Document;

public interface FileResourceBinaryRepository {

  void assertReadability(FileResource resource)
      throws TechnicalException, ResourceNotFoundException;

  FileResource find(String uuid, MimeType mimeType)
      throws TechnicalException, ResourceNotFoundException;

  byte[] getAsBytes(FileResource resource) throws TechnicalException, ResourceNotFoundException;

  Document getAsDocument(FileResource resource)
      throws TechnicalException, ResourceNotFoundException;

  InputStream getInputStream(FileResource resource)
      throws TechnicalException, ResourceNotFoundException;

  FileResource save(FileResource fileResource, InputStream binaryData) throws TechnicalException;

  FileResource save(FileResource fileResource, String input, Charset charset)
      throws TechnicalException;
}
