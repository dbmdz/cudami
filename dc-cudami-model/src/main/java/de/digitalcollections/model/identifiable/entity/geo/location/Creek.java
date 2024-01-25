package de.digitalcollections.model.identifiable.entity.geo.location;

import lombok.experimental.SuperBuilder;

/** A small stream. see https://www.wikidata.org/wiki/Q63565252 "Bach; kleiner Fluss" */
@SuperBuilder(buildMethodName = "prebuild")
public class Creek extends GeoLocation {

  public abstract static class CreekBuilder<C extends Creek, B extends CreekBuilder<C, B>>
      extends GeoLocationBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }
  }

  public Creek() {
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
    this.geoLocationType = GeoLocationType.CREEK;
  }

  @Override
  public String toString() {
    return "Creek [coordinateLocation="
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
