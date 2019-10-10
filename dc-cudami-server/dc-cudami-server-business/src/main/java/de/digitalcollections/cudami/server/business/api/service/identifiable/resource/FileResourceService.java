package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import java.io.InputStream;

public interface FileResourceService extends IdentifiableService<FileResource> {

  default FileResource createByContentTypeAndFilename(String contentType, String filename) {
    return createByMimeTypeAndFilename(MimeType.fromTypename(contentType), filename);
  }

  default FileResource createByFilename(String filename) {
    MimeType mimeType = MimeType.fromFilename(filename);
    FileResource result = createByMimeType(mimeType);
    result.setFilename(filename);
    return result;
  }

  default FileResource createByFilenameExtension(String filenameExtension) {
    MimeType mimeType = MimeType.fromExtension(filenameExtension);
    FileResource result = createByMimeType(mimeType);
    return result;
  }

  FileResource createByMimeType(MimeType mimeType);

  default FileResource createByMimeTypeAndFilename(MimeType mimeType, String filename) {
    FileResource result = createByMimeType(mimeType);
    result.setFilename(filename);
    return result;
  }

  FileResource save(FileResource fileResource, InputStream binaryData)
      throws IdentifiableServiceException;

  FileResource getByIdentifier(String namespace, String id) throws IdentifiableServiceException;
}
