package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.model.exception.ResourceIOException;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.w3c.dom.Document;

public interface FileResourceBinaryRepository {

  void assertReadability(FileResource resource)
      throws ResourceIOException, ResourceNotFoundException;

  FileResource find(String uuid, MimeType mimeType)
      throws ResourceIOException, ResourceNotFoundException;

  byte[] getAsBytes(FileResource resource) throws ResourceIOException, ResourceNotFoundException;

  Document getAsDocument(FileResource resource)
      throws ResourceIOException, ResourceNotFoundException;

  InputStream getInputStream(FileResource resource)
      throws ResourceIOException, ResourceNotFoundException;

  FileResource save(FileResource fileResource, InputStream binaryData) throws ResourceIOException;

  FileResource save(FileResource fileResource, String input, Charset charset)
      throws ResourceIOException;
}
