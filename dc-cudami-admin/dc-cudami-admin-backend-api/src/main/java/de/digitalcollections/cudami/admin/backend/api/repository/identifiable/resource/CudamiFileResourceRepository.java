package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import java.io.InputStream;

/**
 * @param <F> resource instance
 */
public interface CudamiFileResourceRepository<F extends FileResource> extends IdentifiableRepository<F> {

  public F save(FileResource fileResource, byte[] bytes);

  public F upload(InputStream inputStream, String filename, String contentType) throws ResourceIOException;
  
  public F upload(byte[] bytes, String filename, String contentType) throws ResourceIOException;
}
