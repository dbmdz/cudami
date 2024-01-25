package de.digitalcollections.model.jackson.mixin.identifiable.entity.geo.location;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;

@JsonDeserialize(as = HumanSettlement.class)
@JsonTypeName("HUMAN_SETTLEMENT")
public interface HumanSettlementMixIn extends GeoLocationMixIn {}
