package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.identifiable.resource.AudioFileResource;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.TextFileResource;
import de.digitalcollections.model.identifiable.resource.VideoFileResource;
import java.util.UUID;

/**
 * Repository for FileResource persistence handling.
 *
 * @param <F> instance of file resource implementation
 */
public interface FileResourceMetadataRepository<F extends FileResource>
    extends IdentifiableRepository<F> {

  default FileResource createByMimeType(MimeType mimeType) {
    if (mimeType == null) {
      return new ApplicationFileResource();
    }
    FileResource result;
    String primaryType = mimeType.getPrimaryType();
    switch (primaryType) {
      case "audio":
        result = new AudioFileResource();
        break;
      case "image":
        result = new ImageFileResource();
        break;
      case "text":
        result = new TextFileResource();
        break;
      case "video":
        result = new VideoFileResource();
        break;
      default:
        result = new ApplicationFileResource();
    }
    result.setMimeType(mimeType);
    // FIXME: don't do it!!! it is needed for doing first binary upload before metadata save (and
    // uuid creation)
    final UUID uuid = UUID.randomUUID();
    result.setUuid(uuid);
    return result;
  }
}
