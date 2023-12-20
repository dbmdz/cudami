package de.digitalcollections.model.identifiable.resource;

public enum FileResourceType {
  APPLICATION,
  AUDIO,
  IMAGE,
  LINKED_DATA,
  TEXT,
  UNDEFINED,
  VIDEO;

  @Override
  public String toString() {
    return name();
  }
}
