package de.digitalcollections.model.identifiable;

/** All {@link Identifiable} top types of this library. */
public enum IdentifiableType {
  ENTITY,
  RESOURCE;

  @Override
  public String toString() {
    return name();
  }
}
