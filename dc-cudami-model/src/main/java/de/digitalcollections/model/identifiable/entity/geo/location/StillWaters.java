package de.digitalcollections.model.identifiable.entity.geo.location;

import lombok.experimental.SuperBuilder;

/**
 * A natural or man-made body of water. see https://www.wikidata.org/wiki/Q337567 "Stillgewässer;
 * natürliche oder künstlich geschaffene Gewässer"
 */
@SuperBuilder(buildMethodName = "prebuild")
public class StillWaters extends GeoLocation {

  public abstract static class StillWatersBuilder<
          C extends StillWaters, B extends StillWatersBuilder<C, B>>
      extends GeoLocationBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }
  }

  public StillWaters() {
    super();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    return true;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  protected void init() {
    super.init();
    this.geoLocationType = GeoLocationType.STILL_WATERS;
  }

  @Override
  public String toString() {
    return "StillWaters [coordinateLocation="
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
