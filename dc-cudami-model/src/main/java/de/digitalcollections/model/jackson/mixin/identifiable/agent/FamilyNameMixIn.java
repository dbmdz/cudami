package de.digitalcollections.model.jackson.mixin.identifiable.agent;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.jackson.mixin.identifiable.IdentifiableMixIn;

@JsonDeserialize(as = FamilyName.class)
@JsonTypeName("FAMILY_NAME")
public interface FamilyNameMixIn extends IdentifiableMixIn {}
