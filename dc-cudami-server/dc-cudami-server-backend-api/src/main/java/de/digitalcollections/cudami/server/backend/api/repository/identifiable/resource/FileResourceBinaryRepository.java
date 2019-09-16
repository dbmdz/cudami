package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.w3c.dom.Document;

public interface FileResourceBinaryRepository {

  void assertReadability(FileResource resource) throws ResourceIOException, ResourceNotFoundException;

  FileResource find(String uuid) throws ResourceIOException, ResourceNotFoundException;

  byte[] getAsBytes(FileResource resource) throws ResourceIOException, ResourceNotFoundException;

  Document getAsDocument(FileResource resource) throws ResourceIOException, ResourceNotFoundException;

  InputStream getInputStream(FileResource resource) throws ResourceIOException, ResourceNotFoundException;

  FileResource save(FileResource fileResource, InputStream binaryData) throws ResourceIOException;

  FileResource save(FileResource fileResource, String input, Charset charset) throws ResourceIOException;
}
