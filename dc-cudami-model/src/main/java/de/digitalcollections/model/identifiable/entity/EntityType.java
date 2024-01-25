package de.digitalcollections.model.identifiable.entity;

/**
 * All entity types cudami can handle
 *
 * @deprecated Use IdentifiableObjectType instead.
 */
@Deprecated(forRemoval = true, since = "10.0.0")
public enum EntityType {
  AGENT,
  ARTICLE,
  AUDIO,
  BOOK,
  COLLECTION,
  CORPORATE_BODY,
  DIGITAL_OBJECT,
  ENTITY,
  EVENT,
  EXPRESSION,
  FAMILY,
  GEOLOCATION,
  HEADWORD_ENTRY,
  IMAGE,
  ITEM,
  MANIFESTATION,
  OBJECT_3D,
  PERSON,
  PLACE,
  PROJECT,
  TOPIC,
  VIDEO,
  WEBSITE,
  WORK;

  @Override
  public String toString() {
    return name();
  }
}
