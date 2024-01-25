package de.digitalcollections.model.jackson.mixin.identifiable.entity.agent;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.agent.Family;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.EntityMixIn;

@JsonDeserialize(as = Family.class)
@JsonTypeName("FAMILY")
public interface FamilyMixIn extends EntityMixIn {}
