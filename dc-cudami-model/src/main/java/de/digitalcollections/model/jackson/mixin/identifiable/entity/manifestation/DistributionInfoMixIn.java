package de.digitalcollections.model.jackson.mixin.identifiable.entity.manifestation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.manifestation.DistributionInfo;

@JsonDeserialize(as = DistributionInfo.class)
@JsonTypeName("DISTRIBUTION_INFO")
public interface DistributionInfoMixIn {

  @JsonIgnore
  public boolean isEmpty();
}
