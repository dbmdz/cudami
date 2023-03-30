package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.UUID;
import org.w3c.dom.Document;

public interface FileResourceBinaryRepository {

  void assertReadability(FileResource resource)
      throws RepositoryException, ResourceNotFoundException;

  default FileResource getByExampleAndMimetype(FileResource fileResource, MimeType mimeType)
      throws RepositoryException, ResourceNotFoundException {
    if (fileResource == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getByExampleAndMimetype(fileResource.getUuid(), mimeType);
  }

  FileResource getByExampleAndMimetype(UUID uuid, MimeType mimeType)
      throws RepositoryException, ResourceNotFoundException;

  byte[] getAsBytes(FileResource resource) throws RepositoryException, ResourceNotFoundException;

  Document getAsDocument(FileResource resource)
      throws RepositoryException, ResourceNotFoundException;

  InputStream getInputStream(FileResource resource)
      throws RepositoryException, ResourceNotFoundException;

  void save(FileResource fileResource, InputStream binaryData) throws RepositoryException;

  void save(FileResource fileResource, String input, Charset charset) throws RepositoryException;
}
