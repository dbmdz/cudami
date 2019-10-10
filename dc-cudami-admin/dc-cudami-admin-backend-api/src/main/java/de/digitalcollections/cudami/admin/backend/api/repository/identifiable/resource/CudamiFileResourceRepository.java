package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import java.io.InputStream;

public interface CudamiFileResourceRepository extends IdentifiableRepository<FileResource> {

  public FileResource save(FileResource fileResource, byte[] bytes);

  public FileResource upload(InputStream inputStream, String filename, String contentType)
      throws ResourceIOException;

  public FileResource upload(byte[] bytes, String filename, String contentType)
      throws ResourceIOException;
}
