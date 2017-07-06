package de.digitalcollections.cudami.model.api.entity;

import de.digitalcollections.core.model.api.MimeType;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

/**
 * BinaryContent is used for binary content like PDF, video, audio, image.
 *
 * @param <ID> unique id specifying instance
 */
public interface BinaryContent<ID extends Serializable> extends Entity<ID> {

  List<ContentNode> getContentNodes();

  void setContentNodes(List<ContentNode> contentNodes);

  long getSizeInBytes();
  
  void setSizeInBytes(long sizeInBytes);
  
  URL getPreviewUrl();
  
  void setPreviewUrl(URL previewUrl);
  
  MimeType getMimeType();
  
  void setMimeType(MimeType mimeType);
}
