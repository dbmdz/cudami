package de.digitalcollections.cudami.model.api.identifiable.resource;

import de.digitalcollections.core.model.api.MimeType;
import java.net.URL;

/**
 * BinaryContent is used for binary content like PDF, video, audio, image.
 */
public interface BinaryContent extends Resource {

  long getSizeInBytes();

  void setSizeInBytes(long sizeInBytes);

  URL getPreviewUrl();

  void setPreviewUrl(URL previewUrl);

  MimeType getMimeType();

  void setMimeType(MimeType mimeType);
}
