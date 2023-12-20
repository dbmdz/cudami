package de.digitalcollections.model.jackson.mixin.identifiable.entity.geo.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.geo.location.Mountain;

@JsonDeserialize(as = Mountain.class)
@JsonTypeName("MOUNTAIN")
public interface MountainMixIn extends GeoLocationMixIn {
  @JsonIgnore
  public int getHeight();
}
