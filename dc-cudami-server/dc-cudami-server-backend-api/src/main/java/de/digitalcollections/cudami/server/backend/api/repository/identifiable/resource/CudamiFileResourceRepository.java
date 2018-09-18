package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.io.InputStream;

/**
 * @param <F> file resource instance
 */
public interface CudamiFileResourceRepository<F extends FileResource> extends ResourceRepository<F> {

  F save(F fileResource, InputStream binaryData);
}
