package de.digitalcollections.model.jackson.mixin.identifiable.entity.agent;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.EntityMixIn;

@JsonDeserialize(as = CorporateBody.class)
@JsonTypeName("CORPORATE_BODY")
public interface CorporateBodyMixIn extends EntityMixIn {}
