package de.digitalcollections.cudami.model.api.identifiable.entity;

/**
 * All {@link Entity} types cudami can handle
 */
public enum EntityType {
  ARTICLE, AUDIO, BOOK, CORPORATION, COLLECTION, CONTENT_TREE, EVENT, IMAGE, OBJECT_3D, PERSON, PLACE, VIDEO, WEBSITE;

  @Override
  public String toString() {
    return name();
  }
}
