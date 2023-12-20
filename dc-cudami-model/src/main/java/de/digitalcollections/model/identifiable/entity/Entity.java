package de.digitalcollections.model.identifiable.entity;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.experimental.SuperBuilder;

/**
 * Entities are uniquely identifiable objects, often also uniquely identifiable outside of this
 * technical system - by additionally having unique identifiers of other systems (like GND-Id,
 * VIAF-ID, etc.). An entity is of a specific entity type, like e.g. cultural object, item,
 * manifestation, work, digital object, person, corporation.
 *
 * <p>Bibliographic entity types for example are defined in the “Functional Requirements for
 * Bibliographic Records” (FRBR; deutsch ‚Funktionale Anforderungen an bibliographische
 * Datensätze‘): see Wikipedia “Functional Requirements for Bibliographic Records”, URLs:
 *
 * <p>https://de.wikipedia.org/wiki/Functional_Requirements_for_Bibliographic_Records
 * https://en.wikipedia.org/wiki/Functional_Requirements_for_Bibliographic_Records
 */
@SuperBuilder(buildMethodName = "prebuild")
public class Entity extends Identifiable {

  protected CustomAttributes customAttributes;

  /** A "navigable" date, required e.g. when you need to the display an entity on a timeline. */
  protected LocalDate navDate;

  protected long refId;

  protected List<LocalizedStructuredContent> notes;

  public Entity() {
    super();
  }

  public void addNotes(LocalizedStructuredContent... notesToAdd) {
    if (notes == null) {
      notes = new ArrayList<>();
    }
    Arrays.stream(notesToAdd).forEachOrdered(note -> notes.add(note));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Entity)) {
      return false;
    }
    Entity entity = (Entity) o;
    return super.equals(o)
        && refId == entity.refId
        && Objects.equals(customAttributes, entity.customAttributes)
        && Objects.equals(navDate, entity.navDate)
        && Objects.equals(notes, entity.notes);
  }

  /**
   * @param attributeName attribute name of custom attribute
   * @return value of custom attribute or null
   */
  public Object getCustomAttribute(String attributeName) {
    if (hasCustomAttribute(attributeName)) {
      return getCustomAttributes().get(attributeName);
    }
    return null;
  }

  /**
   * @return custom attributes
   */
  public CustomAttributes getCustomAttributes() {
    return customAttributes;
  }

  /**
   * @deprecated Use {@link Identifiable#getType()} and {@link
   *     Identifiable#getIdentifiableObjectType()} instead.
   * @return the type of the entity (one of the types this system can manage, defined in enum
   *     EntityType).
   */
  @Deprecated(forRemoval = true, since = "10.0.0")
  public EntityType getEntityType() {
    if (IdentifiableType.RESOURCE == getType()) {
      return null;
    }
    switch (identifiableObjectType) {
      case CANYON:
      case CAVE:
      case CONTINENT:
      case COUNTRY:
      case CREEK:
      case GEO_LOCATION:
      case HUMAN_SETTLEMENT:
      case LAKE:
      case MOUNTAIN:
      case OCEAN:
      case RIVER:
      case SEA:
      case STILL_WATERS:
      case VALLEY:
        return EntityType.GEOLOCATION;
      default:
        // as both enum have String identical enum values in all other cases, we can simply map by
        // String:
        return EntityType.valueOf(getIdentifiableObjectType().toString());
    }
  }

  /**
   * @return a date for "navigation" purposes, e.g. a timeline
   */
  public LocalDate getNavDate() {
    return navDate;
  }

  /** Arbitrary notes and remarks */
  public List<LocalizedStructuredContent> getNotes() {
    return notes;
  }

  /**
   * Get the system wide unique reference id. Makes it possible to create very short permanent URIs
   * by using a number.
   *
   * @return system wide unique entity reference id
   */
  public long getRefId() {
    return refId;
  }

  /**
   * @param attributeName attribute name for lookup
   * @return true if map contains custom attribute of given name
   */
  private boolean hasCustomAttribute(String attributeName) {
    return customAttributes != null && customAttributes.containsKey(attributeName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), customAttributes, navDate, notes, refId);
  }

  @Override
  protected void init() {
    super.init();
    this.type = IdentifiableType.ENTITY;
    if (notes == null) notes = new ArrayList<>(0);
  }

  /**
   * Sets custom Attribute of given name to given value (overwriting existing value).
   *
   * @param attributeName name of custom attribute
   * @param attributeValue value of custom attibute
   */
  public void setCustomAttribute(String attributeName, Object attributeValue) {
    if (customAttributes == null) {
      customAttributes = new CustomAttributes();
    }
    customAttributes.put(attributeName, attributeValue);
  }

  /**
   * Set custom attributes (name, value).
   *
   * @param customAttributes custom attributes to be set
   */
  public void setCustomAttributes(CustomAttributes customAttributes) {
    this.customAttributes = customAttributes;
  }

  /**
   * Sets the "navigation" date
   *
   * @param navDate the "navigation" date
   */
  public void setNavDate(LocalDate navDate) {
    this.navDate = navDate;
  }

  public void setNotes(List<LocalizedStructuredContent> notes) {
    this.notes = notes;
  }

  /**
   * @param refId system wide unique entity reference id.
   */
  public void setRefId(long refId) {
    this.refId = refId;
  }

  public abstract static class EntityBuilder<C extends Entity, B extends EntityBuilder<C, B>>
      extends IdentifiableBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      return c;
    }

    public B customAttribute(String key, Object value) {
      if (this.customAttributes == null) {
        this.customAttributes = new CustomAttributes();
      }
      this.customAttributes.put(key, value);
      return self();
    }

    public B navDate(String navDate) {
      this.navDate = LocalDate.parse(navDate);
      return self();
    }

    public B note(LocalizedStructuredContent content) {
      if (notes == null) {
        notes = new ArrayList<>();
      }
      notes.add(content);
      return self();
    }
  }
}
