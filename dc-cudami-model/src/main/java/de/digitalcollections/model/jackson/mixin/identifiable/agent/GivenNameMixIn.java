package de.digitalcollections.model.jackson.mixin.identifiable.agent;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.jackson.mixin.identifiable.IdentifiableMixIn;

@JsonDeserialize(as = GivenName.class)
@JsonTypeName("GIVEN_NAME")
public interface GivenNameMixIn extends IdentifiableMixIn {}
