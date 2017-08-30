package de.digitalcollections.cudami.model.impl;

import de.digitalcollections.cudami.model.api.Thumbnail;
import de.digitalcollections.iiif.image.model.api.enums.Version;

public class ThumbnailImpl implements Thumbnail {

  private Version iiifImageApiVersion;
  private String iiifImageServiceUri;
  private String url;

  public ThumbnailImpl() {

  }

  public ThumbnailImpl(Version iiifImageApiVersion, String iiifImageServiceUri) {
    this.iiifImageApiVersion = iiifImageApiVersion;
    this.iiifImageServiceUri = iiifImageServiceUri;
  }

  public ThumbnailImpl(String context, String iiifImageServiceUri) {
    this(Version.getVersion(context), iiifImageServiceUri);
  }

  public ThumbnailImpl(String url) {
    this.url = url;
  }

  @Override
  public Version getIiifImageApiVersion() {
    return iiifImageApiVersion;
  }

  @Override
  public void setIiifImageApiVersion(Version iiifImageApiVersion) {
    this.iiifImageApiVersion = iiifImageApiVersion;
  }

  @Override
  public String getIiifImageServiceUri() {
    return iiifImageServiceUri;
  }

  @Override
  public void setIiifImageServiceUri(String iiifImageServiceUri) {
    if (iiifImageServiceUri != null) {
      iiifImageServiceUri = iiifImageServiceUri.trim();
      if (iiifImageServiceUri.endsWith("/")) {
        iiifImageServiceUri = iiifImageServiceUri.substring(0, iiifImageServiceUri.lastIndexOf("/"));
      }
    }
    this.iiifImageServiceUri = iiifImageServiceUri;
  }

  @Override
  public String getUrl() {
//    if (iiifImageServiceUri != null && iiifImageApiVersion != null) {
//      if (IiifImageApiVersion.V1_1 == iiifImageApiVersion) {
//        setUrl(iiifImageServiceUri + "/full/,90/0/native.jpg");
//      } else if (IiifImageApiVersion.V2 == iiifImageApiVersion) {
//        setUrl(iiifImageServiceUri + "/full/,90/0/default.jpg");
//      }
//    }
    return url;
  }

  @Override
  public void setUrl(String url) {
    this.url = url;
  }
}
