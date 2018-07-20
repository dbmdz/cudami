package de.digitalcollections.cudami.model.api.identifiable.resource;

/**
 * All {@link Resource} types cudami can handle
 */
public enum ResourceType {
  BINARY_CONTENT, CONTENT_NODE, IIIF_IMAGE, IIIF_MANIFEST, WEBPAGE;

  @Override
  public String toString() {
    return name();
  }
}
