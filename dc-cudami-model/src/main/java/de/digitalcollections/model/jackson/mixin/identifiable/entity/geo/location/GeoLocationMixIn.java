package de.digitalcollections.model.jackson.mixin.identifiable.entity.geo.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.digitalcollections.model.identifiable.entity.geo.location.Canyon;
import de.digitalcollections.model.identifiable.entity.geo.location.Cave;
import de.digitalcollections.model.identifiable.entity.geo.location.Continent;
import de.digitalcollections.model.identifiable.entity.geo.location.Country;
import de.digitalcollections.model.identifiable.entity.geo.location.Creek;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.geo.location.Lake;
import de.digitalcollections.model.identifiable.entity.geo.location.Mountain;
import de.digitalcollections.model.identifiable.entity.geo.location.Ocean;
import de.digitalcollections.model.identifiable.entity.geo.location.River;
import de.digitalcollections.model.identifiable.entity.geo.location.Sea;
import de.digitalcollections.model.identifiable.entity.geo.location.StillWaters;
import de.digitalcollections.model.identifiable.entity.geo.location.Valley;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.EntityMixIn;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "geoLocationType",
    visible = true)
@JsonSubTypes({
  // need to be uppercase (and included as EXISTING_PROPERTY) to reuse enum field values for
  // deserializing:
  @JsonSubTypes.Type(value = Canyon.class, name = "CANYON"),
  @JsonSubTypes.Type(value = Cave.class, name = "CAVE"),
  @JsonSubTypes.Type(value = Continent.class, name = "CONTINENT"),
  @JsonSubTypes.Type(value = Country.class, name = "COUNTRY"),
  @JsonSubTypes.Type(value = Creek.class, name = "CREEK"),
  @JsonSubTypes.Type(value = GeoLocation.class, name = "GEOLOCATION"),
  @JsonSubTypes.Type(value = HumanSettlement.class, name = "HUMAN_SETTLEMENT"),
  @JsonSubTypes.Type(value = Lake.class, name = "LAKE"),
  @JsonSubTypes.Type(value = Mountain.class, name = "MOUNTAIN"),
  @JsonSubTypes.Type(value = Ocean.class, name = "OCEAN"),
  @JsonSubTypes.Type(value = River.class, name = "RIVER"),
  @JsonSubTypes.Type(value = Sea.class, name = "SEA"),
  @JsonSubTypes.Type(value = StillWaters.class, name = "STILL_WATERS"),
  @JsonSubTypes.Type(value = Valley.class, name = "VALLEY")
})
public interface GeoLocationMixIn extends EntityMixIn {

  @JsonIgnore
  public Double getLatitude();

  @JsonIgnore
  public Double getLongitude();
}
