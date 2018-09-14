package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.model.api.identifiable.resource.FileResource;

/**
 * @param <F> file resource instance
 */
public interface FileResourceRepository<F extends FileResource> extends ResourceRepository<F> {

  F save(F fileResource, byte[] binaryData);
}
