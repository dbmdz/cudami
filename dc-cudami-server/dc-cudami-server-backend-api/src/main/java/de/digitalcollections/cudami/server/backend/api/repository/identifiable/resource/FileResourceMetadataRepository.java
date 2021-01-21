package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.resource.FileResource;

/**
 * Repository for FileResource persistence handling.
 *
 * @param <F> instance of file resource implementation
 */
public interface FileResourceMetadataRepository<F extends FileResource>
    extends IdentifiableRepository<F> {}
