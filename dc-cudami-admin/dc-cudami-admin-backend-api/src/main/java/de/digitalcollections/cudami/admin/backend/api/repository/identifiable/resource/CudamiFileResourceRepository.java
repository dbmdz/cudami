package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.resource.FileResource;

/**
 * @param <F> resource instance
 */
public interface CudamiFileResourceRepository<F extends FileResource> extends IdentifiableRepository<F> {

  public F save(FileResource fileResource, byte[] bytes);

}
