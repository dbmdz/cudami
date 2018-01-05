package de.digitalcollections.cudami.model.impl.identifiable.resource;

import de.digitalcollections.core.model.api.MimeType;
import de.digitalcollections.cudami.model.api.identifiable.resource.BinaryContent;
import de.digitalcollections.cudami.model.api.identifiable.resource.ResourceType;
import java.net.URL;

public class BinaryContentImpl extends ResourceImpl implements BinaryContent {

  private MimeType mimeType;
  private URL previewUrl;
  private long sizeInBytes;

  public BinaryContentImpl() {
    super();
    this.resourceType = ResourceType.BINARY_CONTENT;
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
