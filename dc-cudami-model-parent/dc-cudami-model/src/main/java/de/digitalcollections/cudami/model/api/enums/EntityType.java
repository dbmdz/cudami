package de.digitalcollections.cudami.model.api.enums;

import de.digitalcollections.cudami.model.api.entity.Entity;

/**
 * All {@link Entity} types cudami can handle
 */
public enum EntityType {
  AUDIO, BOOK, CONTENT_NODE, CORPORATION, ENTITY_COLLECTION, EVENT, EXHIBITION, IMAGE, OBJECT_3D, PERSON, PLACE, TEXT, VIDEO, WEBSITE;

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
