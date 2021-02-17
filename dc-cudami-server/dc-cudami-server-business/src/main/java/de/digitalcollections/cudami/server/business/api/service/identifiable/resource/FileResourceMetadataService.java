package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResource;

public interface FileResourceMetadataService<F extends FileResource>
    extends IdentifiableService<F> {

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
}
