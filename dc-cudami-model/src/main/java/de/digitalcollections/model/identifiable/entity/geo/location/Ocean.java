package de.digitalcollections.model.identifiable.entity.geo.location;

import lombok.experimental.SuperBuilder;

/**
 * An ocean is a large body of saline water (german = "Ozean"). see
 * https://www.wikidata.org/wiki/Q9430 "größte Meere der Erde, Weltmeere"
 */
@SuperBuilder(buildMethodName = "prebuild")
public class Ocean extends GeoLocation {

  public abstract static class OceanBuilder<C extends Ocean, B extends OceanBuilder<C, B>>
      extends GeoLocationBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }
  }

  public Ocean() {
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
    this.geoLocationType = GeoLocationType.OCEAN;
  }

  @Override
  public String toString() {
    return "Ocean [coordinateLocation="
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
