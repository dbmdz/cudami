package de.digitalcollections.cudami.model.api.identifiable.resource;

/**
 * All {@link Resource} types cudami can handle
 */
public enum ResourceType {
  BINARY_CONTENT, CONTENTBLOCKS_CONTAINER, CONTENTNODE, IIIF_IMAGE, IIIF_MANIFEST, WEBPAGE;

  @Override
  public String toString() {
    return name();
  }
}
