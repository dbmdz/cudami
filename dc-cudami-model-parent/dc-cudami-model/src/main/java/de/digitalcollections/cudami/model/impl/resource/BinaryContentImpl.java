package de.digitalcollections.cudami.model.impl.resource;

import de.digitalcollections.core.model.api.MimeType;
import de.digitalcollections.cudami.model.api.resource.BinaryContent;
import de.digitalcollections.cudami.model.impl.identifiable.IdentifiableImpl;
import java.net.URL;

public class BinaryContentImpl extends IdentifiableImpl implements BinaryContent {

  private MimeType mimeType;
  private URL previewUrl;
  private long sizeInBytes;

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
