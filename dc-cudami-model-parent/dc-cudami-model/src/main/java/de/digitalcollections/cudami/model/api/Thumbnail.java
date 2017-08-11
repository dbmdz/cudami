package de.digitalcollections.cudami.model.api;

import de.digitalcollections.iiif.image.model.api.enums.Version;

public interface Thumbnail {

  Version getIiifImageApiVersion();

  String getIiifImageServiceUri();

  String getUrl();

  void setIiifImageApiVersion(Version iiifImageApiVersion);

  void setIiifImageServiceUri(String iiifImageServiceUri);

  void setUrl(String url);
}
