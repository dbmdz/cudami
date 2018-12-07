package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.io.InputStream;

public interface CudamiFileResourceRepository extends IdentifiableRepository<FileResource> {

  FileResource save(FileResource fileResource, InputStream binaryData);
}
