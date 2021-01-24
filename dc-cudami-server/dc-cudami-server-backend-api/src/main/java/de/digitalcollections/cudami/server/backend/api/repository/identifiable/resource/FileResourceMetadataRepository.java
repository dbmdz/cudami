package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.impl.identifiable.resource.ApplicationFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.AudioFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.TextFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.VideoFileResourceImpl;
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
      return new ApplicationFileResourceImpl();
    }
    FileResource result;
    String primaryType = mimeType.getPrimaryType();
    switch (primaryType) {
      case "audio":
        result = new AudioFileResourceImpl();
        break;
      case "image":
        result = new ImageFileResourceImpl();
        break;
      case "text":
        result = new TextFileResourceImpl();
        break;
      case "video":
        result = new VideoFileResourceImpl();
        break;
      default:
        result = new ApplicationFileResourceImpl();
    }
    result.setMimeType(mimeType);
    // FIXME: don't do it!!! it is needed for doing first binary upload before metadata save (and
    // uuid creation)
    final UUID uuid = UUID.randomUUID();
    result.setUuid(uuid);
    return result;
  }
}
