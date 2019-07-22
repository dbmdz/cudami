package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;

public interface FileResourceMetadataRepository extends IdentifiableRepository<FileResource> {

  FileResource createByMimeType(MimeType mimeType);
}
