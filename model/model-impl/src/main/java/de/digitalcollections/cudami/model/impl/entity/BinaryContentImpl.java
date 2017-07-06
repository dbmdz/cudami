package de.digitalcollections.cudami.model.impl.entity;

import de.digitalcollections.cudami.model.api.entity.BinaryContent;
import de.digitalcollections.cudami.model.api.entity.ContentNode;
import de.digitalcollections.core.model.api.MimeType;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

public class BinaryContentImpl<ID extends Serializable> extends EntityImpl<ID> implements BinaryContent<ID> {

  private List<ContentNode> contentNodes;
  private MimeType mimeType;
  private URL previewUrl;
  private long sizeInBytes;

  @Override
  public List<ContentNode> getContentNodes() {
    return contentNodes;
  }

  @Override
  public void setContentNodes(List<ContentNode> contentNodes) {
    this.contentNodes = contentNodes;
  }

  @Override
  public long getSizeInBytes() {
    return sizeInBytes;
  }

  @Override
  public void setSizeInBytes(long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }

  @Override
  public URL getPreviewUrl() {
    return previewUrl;
  }

  @Override
  public void setPreviewUrl(URL previewUrl) {
    this.previewUrl = previewUrl;
  }

  @Override
  public MimeType getMimeType() {
    return mimeType;
  }

  @Override
  public void setMimeType(MimeType mimeType) {
    this.mimeType = mimeType;
  }
}
