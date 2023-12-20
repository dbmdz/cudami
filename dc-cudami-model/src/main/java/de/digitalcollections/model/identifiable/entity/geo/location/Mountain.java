package de.digitalcollections.model.identifiable.entity.geo.location;

import lombok.experimental.SuperBuilder;

/**
 * A mountain is a large landform that stretches above the surrounding land. see
 * https://www.wikidata.org/wiki/Q8502
 */
@SuperBuilder(buildMethodName = "prebuild")
public class Mountain extends GeoLocation {

  public abstract static class MountainBuilder<C extends Mountain, B extends MountainBuilder<C, B>>
      extends GeoLocationBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }
  }

  public Mountain() {
    super();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    return true;
  }

  /**
   * @return height of mountain above sea level in meters.
   */
  public int getHeight() {
    Object height = getCustomAttribute("height");
    if (height == null) {
      return -1;
    }
    return (int) height;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  protected void init() {
    super.init();
    this.geoLocationType = GeoLocationType.MOUNTAIN;
  }

  /**
   * @param height height of mountain above sea level in meters
   */
  public void setHeight(int height) {
    setCustomAttribute("height", height);
  }

  @Override
  public String toString() {
    return "Mountain [coordinateLocation="
        + coordinateLocation
        + ", geoLocationType="
        + geoLocationType
        + ", name="
        + name
        + ", nameLocalesOfOriginalScripts="
        + nameLocalesOfOriginalScripts
        + ", customAttributes="
        + customAttributes
        + ", navDate="
        + navDate
        + ", refId="
        + refId
        + ", notes="
        + notes
        + ", description="
        + description
        + ", identifiableObjectType="
        + identifiableObjectType
        + ", identifiers="
        + identifiers
        + ", label="
        + label
        + ", localizedUrlAliases="
        + localizedUrlAliases
        + ", previewImage="
        + previewImage
        + ", previewImageRenderingHints="
        + previewImageRenderingHints
        + ", subjects="
        + subjects
        + ", tags="
        + tags
        + ", type="
        + type
        + ", created="
        + created
        + ", lastModified="
        + lastModified
        + ", uuid="
        + uuid
        + "]";
  }
}
