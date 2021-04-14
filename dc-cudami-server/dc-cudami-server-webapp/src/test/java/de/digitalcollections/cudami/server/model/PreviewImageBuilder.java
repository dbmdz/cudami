package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResourceType;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

public class PreviewImageBuilder {

  ImageFileResource previewImage;

  public PreviewImageBuilder() {
    previewImage = new ImageFileResource();
    previewImage.setFileResourceType(FileResourceType.IMAGE);
    previewImage.setMimeType(MimeType.MIME_IMAGE);
  }

  public PreviewImageBuilder(String uuid) {
    this();
    previewImage.setUuid(UUID.fromString(uuid));
  }

  public ImageFileResource build() {
    previewImage.setFileResourceType(FileResourceType.IMAGE);
    if (previewImage.getMimeType() == null) {
      previewImage.setMimeType(MimeType.MIME_IMAGE);
    }
    return previewImage;
  }

  public PreviewImageBuilder withFileName(String fileName) {
    previewImage.setFilename(fileName);
    return this;
  }

  public PreviewImageBuilder withUri(String uri) {
    previewImage.setUri(URI.create(uri));
    return this;
  }

  public PreviewImageBuilder withMimeType(MimeType mimeType) {
    previewImage.setMimeType(mimeType);
    return this;
  }

  public PreviewImageBuilder withHttpBaseUrl(String httpBaseUrl) {
    try {
      previewImage.setHttpBaseUrl(new URL(httpBaseUrl));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    return this;
  }
}
