package de.digitalcollections.model.identifiable.entity.geo.location;

import de.digitalcollections.model.geo.CoordinateLocation;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.NamedEntity;
import de.digitalcollections.model.text.LocalizedText;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.experimental.SuperBuilder;

/** A location located on earth. */
@SuperBuilder(buildMethodName = "prebuild")
public class GeoLocation extends Entity implements NamedEntity {

  protected CoordinateLocation coordinateLocation;
  protected GeoLocationType geoLocationType;
  protected LocalizedText name;
  protected Set<Locale> nameLocalesOfOriginalScripts;

  public GeoLocation() {
    super();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof GeoLocation)) {
      return false;
    }
    GeoLocation other = (GeoLocation) obj;
    return this == other
        || super.equals(obj)
            && Objects.equals(coordinateLocation, other.coordinateLocation)
            && geoLocationType == other.geoLocationType
            && Objects.equals(name, other.name)
            && Objects.equals(nameLocalesOfOriginalScripts, other.nameLocalesOfOriginalScripts);
  }

  @Override
  public int hashCode() {
    return super.hashCode()
        + Objects.hash(coordinateLocation, geoLocationType, name, nameLocalesOfOriginalScripts)
        + 18;
  }

  public CoordinateLocation getCoordinateLocation() {
    return coordinateLocation;
  }

  public GeoLocationType getGeoLocationType() {
    return geoLocationType;
  }

  public Double getLatitude() {
    if (getCoordinateLocation() == null) {
      return null;
    }
    return getCoordinateLocation().getLatitude();
  }

  public Double getLongitude() {
    if (getCoordinateLocation() == null) {
      return null;
    }
    return getCoordinateLocation().getLongitude();
  }

  @Override
  public LocalizedText getName() {
    return name;
  }

  @Override
  public Set<Locale> getNameLocalesOfOriginalScripts() {
    return nameLocalesOfOriginalScripts;
  }

  @Override
  protected void init() {
    super.init();
    this.geoLocationType = GeoLocationType.GEOLOCATION;
    if (nameLocalesOfOriginalScripts == null) nameLocalesOfOriginalScripts = new HashSet<>(0);
  }

  public void setCoordinateLocation(CoordinateLocation coordinateLocation) {
    this.coordinateLocation = coordinateLocation;
  }

  public void setGeoLocationType(GeoLocationType geoLocationType) {
    this.geoLocationType = geoLocationType;
  }

  @Override
  public void setName(LocalizedText name) {
    this.name = name;
  }

  @Override
  public void setNameLocalesOfOriginalScripts(Set<Locale> localesOfOriginalScripts) {
    this.nameLocalesOfOriginalScripts = localesOfOriginalScripts;
  }

  @Override
  public String toString() {
    return "GeoLocation [coordinateLocation="
        + coordinateLocation
        + ", geoLocationType="
        + geoLocationType
        + ", name="
        + name
        + ", nameLocalesOfOriginalScripts="
        + nameLocalesOfOriginalScripts
        + ", created="
        + created
        + ", customAttributes="
        + customAttributes
        + ", description="
        + description
        + ", identifiableObjectType="
        + identifiableObjectType
        + ", identifiers="
        + identifiers
        + ", label="
        + label
        + ", lastModified="
        + lastModified
        + ", localizedUrlAliases="
        + localizedUrlAliases
        + ", navDate="
        + navDate
        + ", notes="
        + notes
        + ", previewImage="
        + previewImage
        + ", previewImageRenderingHints="
        + previewImageRenderingHints
        + ", refId="
        + refId
        + ", tags="
        + tags
        + ", type="
        + type
        + ", uuid="
        + uuid
        + "]";
  }

  public abstract static class GeoLocationBuilder<
          C extends GeoLocation, B extends GeoLocationBuilder<C, B>>
      extends EntityBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }
  }
}
