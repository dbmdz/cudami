package de.digitalcollections.cudami.admin.business.api.service.identifiable.resource;

import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import java.io.InputStream;

public interface FileResourceBinaryService {

  public FileResource upload(InputStream inputStream, String filename, String contentType)
      throws ResourceIOException;

  public FileResource upload(byte[] bytes, String filename, String contentType)
      throws ResourceIOException;
}
