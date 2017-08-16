package de.digitalcollections.cudami.model.api;

import de.digitalcollections.core.model.api.MimeType;
import de.digitalcollections.cudami.model.api.entity.ContentNode;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

/**
 * BinaryContent is used for binary content like PDF, video, audio, image.
 * @param <ID> unique serializable identifier
 */
public interface BinaryContent<ID extends Serializable> {

  List<ContentNode> getContentNodes();

  void setContentNodes(List<ContentNode> contentNodes);

  long getSizeInBytes();

  void setSizeInBytes(long sizeInBytes);

  URL getPreviewUrl();

  void setPreviewUrl(URL previewUrl);

  MimeType getMimeType();

  void setMimeType(MimeType mimeType);
}
