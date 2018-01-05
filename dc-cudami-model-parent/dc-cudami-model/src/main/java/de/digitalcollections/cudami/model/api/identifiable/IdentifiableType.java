package de.digitalcollections.cudami.model.api.identifiable;

/**
 * All {@link Identifiable} types cudami can handle
 */
public enum IdentifiableType {
  ENTITY, RESOURCE;

  @Override
  public String toString() {
    return name();
  }
}
