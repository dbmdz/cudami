package de.digitalcollections.cudami.model.api.identifiable.resource;

import de.digitalcollections.iiif.image.model.api.enums.Version;

public interface IiifImage extends Resource {

  Version getIiifImageApiVersion();

  String getIiifImageServiceUri();

  String getUrl();

  void setIiifImageApiVersion(Version iiifImageApiVersion);

  void setIiifImageServiceUri(String iiifImageServiceUri);

  void setUrl(String url);
}
